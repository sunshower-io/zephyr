package io.sunshower.kernel.core.lifecycle;

import static io.sunshower.kernel.core.lifecycle.DefaultKernelLifecycle.LifecycleProcessHolder.stopInstance;
import static io.sunshower.kernel.core.lifecycle.DefaultKernelLifecycle.LifecycleProcessHolder.stopPlugins;

import io.sunshower.gyre.Scope;
import io.sunshower.kernel.concurrency.*;
import io.sunshower.kernel.concurrency.Process;
import io.sunshower.kernel.core.Kernel;
import io.sunshower.kernel.core.KernelLifecycle;
import io.sunshower.kernel.core.SunshowerKernel;
import io.sunshower.kernel.misc.SuppressFBWarnings;
import io.sunshower.kernel.module.ModuleLifecycle;
import io.sunshower.kernel.module.ModuleLifecycleChangeGroup;
import io.sunshower.kernel.module.ModuleLifecycleChangeRequest;
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

  private final AtomicReference<State> state;
  private SunshowerKernel kernel;
  private Scheduler<String> scheduler;

  @Inject
  public DefaultKernelLifecycle(SunshowerKernel kernel, Scheduler<String> scheduler) {
    this.kernel = kernel;
    this.scheduler = scheduler;
    this.state = new AtomicReference<>(State.Stopped);
  }

  @Override
  public State getState() {
    return state.get();
  }

  @Override
  public CompletionStage<Process<String>> stop() {
    return scheduler.submit(stopPlugins(kernel)).thenCompose(this::doStop);
  }

  @Override
  public CompletionStage<Process<String>> start() {
    this.state.set(State.Starting);
    val a = scheduler.submit(LifecycleProcessHolder.startInstance(kernel));
    a.thenRun(() -> this.state.set(State.Running));
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
    r.thenRun(() -> this.state.set(State.Stopped));
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
