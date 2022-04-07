package io.zephyr.kernel.core.lifecycle;

import static io.zephyr.kernel.core.lifecycle.DefaultKernelLifecycle.LifecycleProcessHolder.stopInstance;
import static io.zephyr.kernel.core.lifecycle.DefaultKernelLifecycle.LifecycleProcessHolder.stopPlugins;

import io.sunshower.checks.SuppressFBWarnings;
import io.sunshower.gyre.Scope;
import io.zephyr.kernel.concurrency.Process;
import io.zephyr.kernel.concurrency.Scheduler;
import io.zephyr.kernel.concurrency.TaskTracker;
import io.zephyr.kernel.concurrency.Tasks;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.core.KernelEventTypes;
import io.zephyr.kernel.core.KernelLifecycle;
import io.zephyr.kernel.core.SunshowerKernel;
import io.zephyr.kernel.events.Events;
import io.zephyr.kernel.module.ModuleLifecycle;
import io.zephyr.kernel.module.ModuleLifecycleChangeGroup;
import io.zephyr.kernel.module.ModuleLifecycleChangeRequest;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;
import javax.inject.Inject;
import lombok.val;

@SuppressFBWarnings
@SuppressWarnings({
  "PMD.UnusedPrivateField",
  "PMD.DataflowAnomalyAnalysis",
  "PMD.AvoidInstantiatingObjectsInLoops"
})
public class DefaultKernelLifecycle implements KernelLifecycle {

  /** mutable state */
  private SunshowerKernel kernel;

  private Scheduler<String> scheduler;

  /** immutable state */
  private final AtomicReference<State> state;

  private final ClassLoader parentClassloader;

  @Inject
  public DefaultKernelLifecycle(
      SunshowerKernel kernel, Scheduler<String> scheduler, ClassLoader parent) {
    this.kernel = kernel;
    this.parentClassloader = parent;
    this.scheduler = scheduler;
    this.state = new AtomicReference<>(State.Stopped);
  }

  @Override
  public ClassLoader getLaunchClassloader() {
    return parentClassloader;
  }

  @Override
  public State getState() {
    return state.get();
  }

  @Override
  public CompletionStage<Process<String>> stop() {
    kernel.dispatchEvent(KernelEventTypes.KERNEL_SHUTDOWN_INITIATED, Events.create(kernel));
    return scheduler.submit(stopPlugins(kernel)).thenCompose(this::doStop);
  }

  @Override
  public CompletionStage<Process<String>> start() {
    val event = Events.create(kernel);
    kernel.dispatchEvent(KernelEventTypes.KERNEL_START_INITIATED, event);
    this.state.set(State.Starting);
    val a = scheduler.submit(LifecycleProcessHolder.startInstance(kernel));
    a.thenRun(
        () -> {
          this.state.set(State.Running);
          kernel.dispatchEvent(KernelEventTypes.KERNEL_START_SUCCEEDED, event);
        });
    return a;
  }

  @Override
  public CompletionStage<Process<String>> setState(State state) {
    if (state == State.Running) {
      return start();
    } else if (state == State.Stopped) {
      return stop();
    }
    return null;
  }

  @SuppressWarnings("PMD.UnusedFormalParameter")
  private TaskTracker<String> doStop(Process<String> taskSets) {
    this.state.set(State.Stopping);
    val r = scheduler.submit(stopInstance(kernel));
    r.thenRun(
        () -> {
          this.state.set(State.Stopped);
          kernel.dispatchEvent(KernelEventTypes.KERNEL_SHUTDOWN_SUCCEEDED, Events.create(kernel));
        });
    return r;
  }

  static final class LifecycleProcessHolder {

    static final Process<String> stopInstance(Kernel kernel) {
      val scope = Scope.root();
      scope.set("SunshowerKernel", kernel);
      return Tasks.newProcess("kernel:stop")
          .withContext(scope)
          .register(new UnloadKernelFilesystemPhase("kernel:stop:filesystem"))
          .register(new UnloadKernelClassloaderPhase("kernel:stop:classloader"))
          .task("kernel:stop:classloader")
          .dependsOn("kernel:stop:filesystem")
          .create();
    }

    static final Process<String> startInstance(Kernel kernel) {
      val scope = Scope.root();
      scope.set("SunshowerKernel", kernel);
      return Tasks.newProcess("kernel:start:filesystem")
          .withContext(scope)
          .register(new KernelModuleListReadPhase("kernel:lifecycle:module:list"))
          .register(new KernelFilesystemCreatePhase("kernel:lifecycle:filesystem:create"))
          .register(new KernelClassLoaderCreationPhase("kernel:lifecycle:classloader"))
          .task("kernel:lifecycle:module:list")
          .dependsOn("kernel:lifecycle:filesystem:create")
          .task("kernel:lifecycle:classloader")
          .dependsOn("kernel:lifecycle:module:list")
          .create();
    }

    public static Process<String> stopPlugins(SunshowerKernel kernel) {

      val running = kernel.getModuleManager().getModules();
      val results = new ModuleLifecycleChangeRequest[running.size()];
      int i = 0;
      for (val r : running) {
        results[i++] =
            new ModuleLifecycleChangeRequest(r.getCoordinate(), ModuleLifecycle.Actions.Stop);
      }
      return kernel
          .getModuleManager()
          .prepare(new ModuleLifecycleChangeGroup(results))
          .getProcess();
    }
  }
}
