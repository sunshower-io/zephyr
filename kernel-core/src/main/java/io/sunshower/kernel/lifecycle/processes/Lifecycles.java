package io.sunshower.kernel.lifecycle.processes;

import io.sunshower.kernel.concurrency.Process;
import io.sunshower.kernel.concurrency.ReductionScope;
import io.sunshower.kernel.concurrency.Tasks;

public class Lifecycles {

  public static final Process<String> start() {
    return Tasks.newProcess("kernel:start:filesystem")
        .withContext(ReductionScope.newContext())
        .register("kernel:lifecycle:module:list", new KernelModuleListReadPhase())
        .register("kernel:lifecycle:filesystem:create", new KernelFilesystemCreatePhase())
        .register("kernel:lifecycle:classloader", new KernelClassLoaderCreationPhase())
        .task("kernel:lifecycle:module:list")
        .dependsOn("kernel:lifecycle:filesystem:create")
        .task("kernel:lifecycle:classloader")
        .dependsOn("kernel:lifecycle:module:list")
        .create();
  }
}
