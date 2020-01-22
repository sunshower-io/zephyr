package io.zephyr.kernel;

import io.zephyr.api.*;
import io.zephyr.kernel.core.DefaultModule;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.events.Event;
import io.zephyr.kernel.events.EventListener;
import io.zephyr.kernel.events.EventType;
import io.zephyr.kernel.events.Events;
import io.zephyr.kernel.misc.SuppressFBWarnings;
import io.zephyr.kernel.status.Status;
import io.zephyr.kernel.status.StatusType;
import java.io.IOException;
import java.util.ServiceConfigurationError;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
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
public class ModuleThread implements Startable, Stoppable, TaskQueue, Runnable {

  static final Logger log = Logger.getLogger("ModuleThread");
  static final String FAILURE_TEMPLATE = "Failed to start plugin ''{0}''.  Reason: ''{1}''";

  final Module module;

  final Kernel kernel;
  /** we need fairness here */
  final ReentrantLock lock;

  final AtomicBoolean running;
  final Condition queueCondition;
  final Condition moduleCondition;
  final BlockingQueue<Runnable> taskQueue;
  final AtomicReference<Thread> moduleThread;

  public ModuleThread(final Module module, final Kernel kernel) {
    if (module.getType() == Module.Type.KernelModule) {
      throw new IllegalStateException("Error: cannot create a module thread for a kernel module");
    }
    ((DefaultModule) module).setTaskQueue(this);
    this.kernel = kernel;
    this.module = module;
    this.lock = new ReentrantLock();
    this.queueCondition = lock.newCondition();
    this.moduleCondition = lock.newCondition();
    this.moduleThread = new AtomicReference<>();
    this.taskQueue = new LinkedBlockingQueue<>();
    this.running = new AtomicBoolean(false);
  }

  @Override
  public void stop() {
    lock.lock();
    try {
      running.set(false);
      queueCondition.signalAll();
      while (running.get()) {
        try {
          moduleCondition.await(100, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
          log.log(Level.INFO, "interrupted", ex);
          break;
        }
      }
      doStop();
    } finally {
      lock.unlock();
    }
    checkLock();
  }

  @Override
  public void start() {
    lock.lock();
    try {
      val thread = new Thread(this, "module-" + module.getCoordinate().toCanonicalForm());
      moduleThread.set(thread);
      thread.start();
      try {
        moduleCondition.await();
      } catch (InterruptedException ex) {
        log.log(Level.INFO, "module thread interrupted", ex);
      }
    } finally {
      lock.unlock();
    }
    checkLock();
  }

  private void checkLock() {
    if (lock.getHoldCount() != 0) {
      if (log.isLoggable(Level.WARNING)) {
        log.warning("lock held " + lock.getHoldCount());
      }
    }
  }

  @Override
  public int getOutstandingTasks() {
    return taskQueue.size();
  }

  @Override
  public <T> CompletionStage<T> schedule(Callable<T> task) {
    lock.lock();
    try {
      val result = new TaskQueueCallable<>(task);
      taskQueue.offer(result);
      queueCondition.signalAll();
      return result;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public CompletionStage<Void> schedule(Runnable task) {
    lock.lock();
    try {
      val result = new TaskQueueRunnable(task);
      taskQueue.offer(task);
      queueCondition.signalAll();
      return result;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void run() {
    performStart();
    while (running.get()) {
      try {
        lock.lock();
        queueCondition.await();
        while (!taskQueue.isEmpty()) {
          val runnable = taskQueue.take();
          runnable.run();
        }
      } catch (InterruptedException ex) {
        log.log(Level.INFO, "module interrupted", ex);
      } finally {
        lock.unlock();
      }
    }
    finalizeModule();
  }

  private void finalizeModule() {
    lock.lock();
    try {
      moduleCondition.signalAll();
    } finally {
      lock.unlock();
    }
  }

  private void performStart() {
    lock.lock();
    try {
      running.set(true);
      try {
        doStart();
      } finally { // don't hang if an exception is thrown
        moduleCondition.signalAll();
      }
    } finally {
      lock.unlock();
    }
  }

  private void doStart() {
    fireStart();
    val coordinate = module.getCoordinate();
    val currentState = module.getLifecycle().getState();
    if (!currentState.isAtLeast(Lifecycle.State.Active)) {
      module.getLifecycle().setState(Lifecycle.State.Starting);
      val loader = module.getModuleClasspath().resolveServiceLoader(PluginActivator.class);
      kernel.getModuleManager().getModuleLoader().check(module);
      val ctx = kernel.createContext(module);
      moduleThread.get().setContextClassLoader(module.getClassLoader());
      for (val activator : loader) {
        try {
          activator.start(ctx);
          ((DefaultModule) module).setActivator(activator);
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
    log.log(Level.INFO, "Reason: ", ex);
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
            module.getActivator().stop(module.getContext());
          }
          ((DefaultModule) module).setActivator(null);
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
      }
    }
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
