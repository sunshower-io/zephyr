package io.zephyr.kernel.concurrency;

import io.zephyr.api.*;
import io.zephyr.kernel.*;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.core.AbstractModule;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.events.Events;
import io.zephyr.kernel.misc.SuppressFBWarnings;
import io.zephyr.kernel.status.Status;
import io.zephyr.kernel.status.StatusType;
import java.io.IOException;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.val;

/** not really sure if this is a good idea or not */
@SuppressWarnings({
  "PMD.DoNotUseThreads",
  "PMD.AvoidFieldNameMatchingTypeName",
  "PMD.UnusedPrivateMethod",
  "PMD.DataflowAnomalyAnalysis"
})
@SuppressFBWarnings
public class ModuleThread implements Startable, Stoppable, TaskQueue, Runnable, VolatileStorage {

  static final Logger log = Logger.getLogger("ModuleThread");
  static final String FAILURE_TEMPLATE = "Failed to start plugin ''{0}''.  Reason: ''{1}''";

  final Module module;

  final Kernel kernel;

  final AtomicBoolean running;
  final BlockingQueue<Runnable> taskQueue;
  final AtomicReference<Thread> moduleThread;
  final InheritableThreadLocal<Map<Object, Object>> context;

  public ModuleThread(final Module module, final Kernel kernel) {
    if (module.getType() == Module.Type.KernelModule) {
      throw new IllegalStateException("Error: cannot create a module thread for a kernel module");
    }
    this.kernel = kernel;
    this.module = module;
    this.moduleThread = new AtomicReference<>();
    this.taskQueue = new LinkedBlockingQueue<>();
    this.running = new AtomicBoolean(false);
    this.context = new InheritableThreadLocal<>();
    context.set(new ConcurrentHashMap<>());
  }

  final Object queueLock = new Object();
  final Object moduleLock = new Object();

  @Override
  public void stop() {
    synchronized (queueLock) {
      running.set(false);
      queueLock.notifyAll();
      while (running.get()) {
        try {
          synchronized (moduleLock) {
            moduleLock.wait(100);
          }
        } catch (InterruptedException ex) {
          log.log(Level.INFO, "interrupted", ex);
          break;
        }
      }
      doStop();
    }
  }

  @Override
  public void start() {
    synchronized (moduleLock) {
      val thread = new Thread(this, "module-" + module.getCoordinate().toCanonicalForm());
      moduleThread.set(thread);
      thread.start();
      try {
        moduleLock.wait();
      } catch (InterruptedException ex) {
        log.log(Level.INFO, "module thread interrupted", ex);
      }
    }
  }

  @Override
  public int getOutstandingTasks() {
    return taskQueue.size();
  }

  @Override
  public <T> CompletionStage<T> schedule(Callable<T> task) {
    synchronized (queueLock) {
      val result = new TaskQueueCallable<>(task);
      taskQueue.offer(result);
      queueLock.notifyAll();
      return result;
    }
  }

  @Override
  public CompletionStage<Void> schedule(Runnable task) {
    synchronized (queueLock) {
      val result = new TaskQueueRunnable(task);
      taskQueue.offer(task);
      queueLock.notifyAll();
      return result;
    }
  }

  @Override
  public void run() {
    performStart();
    while (running.get()) {
      try {
        synchronized (queueLock) {
          queueLock.wait(100);
        }
        while (!taskQueue.isEmpty()) {
          val runnable = taskQueue.take();
          runnable.run();
        }
      } catch (InterruptedException ex) {
        log.log(Level.INFO, "module interrupted", ex);
      }
    }
    finalizeModule();
  }

  private void finalizeModule() {
    synchronized (moduleLock) {
      moduleLock.notifyAll();
    }
  }

  private void performStart() {
    synchronized (moduleLock) {
      running.set(true);
      try {
        doStart();
      } finally { // don't hang if an exception is thrown
        moduleLock.notifyAll();
      }
    }
  }

  private void doStart() {
    fireStart();
    val coordinate = module.getCoordinate();
    val currentState = module.getLifecycle().getState();
    if (!currentState.isAtLeast(Lifecycle.State.Active)) {
      module.getLifecycle().setState(Lifecycle.State.Starting);
      kernel.getModuleManager().getModuleLoader().check(module);
      val loader = module.getModuleClasspath().resolveServiceLoader(ModuleActivator.class);
      val ctx = kernel.createContext(module, this);
      moduleThread.get().setContextClassLoader(module.getClassLoader());
      for (val activator : loader) {
        try {
          activator.start(ctx);
          ((AbstractModule) module).setActivator(activator);
          break;
        } catch (Exception | ServiceConfigurationError | LinkageError ex) {
          handleFailure(coordinate, ex);
          return;
        }
      }
      fireStarted();
      module.getLifecycle().setState(Lifecycle.State.Active);
    }
  }

  private void fireStarted() {
    kernel.dispatchEvent(
        ModuleEvents.STARTED,
        Events.create(
            module,
            Status.resolvable(
                StatusType.PROGRESSING, "Successfully started module " + module.getCoordinate())));
  }

  private void fireStart() {
    kernel.dispatchEvent(
        ModuleEvents.STARTING,
        Events.create(module, Status.resolvable(StatusType.PROGRESSING, "Starting module...")));
  }

  private void handleFailure(Coordinate coordinate, Throwable ex) {
    kernel.dispatchEvent(
        ModuleEvents.START_FAILED,
        Events.create(
            module, StatusType.FAILED.unresolvable(FAILURE_TEMPLATE, coordinate, ex.getMessage())));
    module.getLifecycle().setState(Lifecycle.State.Failed);
    log.log(Level.WARNING, FAILURE_TEMPLATE, new Object[] {coordinate, ex.getMessage()});
    log.log(Level.FINE, "Reason: ", ex);
  }

  private void doStop() {
    val currentState = module.getLifecycle().getState();
    if (currentState == Lifecycle.State.Resolved) {
      try {
        module.getFileSystem().close();
      } catch (IOException ex) {
        module.getLifecycle().setState(Lifecycle.State.Failed);
        throw new PluginException(ex);
      }
    }
    if (currentState == Lifecycle.State.Active) { // // TODO: 11/11/19 handle Failed
      try {
        module.getLifecycle().setState(Lifecycle.State.Stopping);
        val activator = module.getActivator();
        try {
          if (activator != null) {
            activator.stop(module.getContext());
          }
          ((AbstractModule) module).setActivator(null);
          module.getFileSystem().close();
          moduleThread.get().setContextClassLoader(null);
        } catch (Exception ex) {
          module.getLifecycle().setState(Lifecycle.State.Failed);
          throw new PluginException(ex);
        }
      } finally {
        if (module.getLifecycle().getState() != Lifecycle.State.Failed) {
          module.getLifecycle().setState(Lifecycle.State.Resolved);
        }
        context.set(null);
      }
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <K, V> V get(K key) {
    synchronized (context) {
      return (V) context.get().get(key);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <K, V> V set(K key, V value) {
    synchronized (context) {
      return (V) context.get().put(key, value);
    }
  }

  @Override
  public <K> boolean contains(K key) {
    synchronized (context) {
      return context.get().containsKey(key);
    }
  }

  @Override
  public void clear() {
    context.get().clear();
  }

  static final class TaskQueueRunnable extends CompletableFuture<Void> implements Runnable {

    final Runnable delegate;

    TaskQueueRunnable(Runnable delegate) {
      this.delegate = delegate;
    }

    @Override
    public void run() {
      delegate.run();
      complete(null);
    }
  }

  static final class TaskQueueCallable<T> extends CompletableFuture<T>
      implements Callable<T>, Runnable {
    final Callable<T> delegate;

    TaskQueueCallable(Callable<T> delegate) {
      this.delegate = delegate;
    }

    @Override
    public T call() throws Exception {
      val result = delegate.call();
      complete(result);
      return result;
    }

    @Override
    public void run() {
      try {
        call();
      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }
    }
  }
}
