package io.zephyr.kernel.launch;

import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.core.KernelLifecycle;
import io.zephyr.kernel.server.Server;
import lombok.SneakyThrows;
import lombok.val;

@SuppressWarnings({
  "PMD.DoNotUseThreads",
  "PMD.AvoidUsingVolatile",
  "PMD.AbstractClassWithoutAbstractMethod"
})
public abstract class CommandTestCase {

  protected volatile KernelLauncher launcher;

  protected Kernel getKernel() {
    if (launcher == null) {
      throw new IllegalStateException("Must start kernel");
    }

    val kernel = launcher.getContext().getService(Kernel.class);
    if (kernel == null) {
      throw new IllegalStateException("looks like you probably ran doRun() instead of runRemote()");
    }
    return kernel;
  }

  protected Server startServer() {
    doRun("-s");
    Server server;
    for (; ; ) {
      server = launcher.getContext().getService(Server.class);
      if (server != null) {
        if (server.isRunning()) {
          break;
        }
      }
    }
    return server;
  }

  protected void runRemote(String... args) {
    val launcher = KernelLauncher.prepare(args);
    val thread = new Thread(launcher::run);
    thread.start();
  }

  protected void doRun(String... args) {
    launcher = KernelLauncher.prepare(args);
    val thread = new Thread(launcher::run);
    thread.start();
  }

  @SneakyThrows
  protected void waitForPluginCount(int count) {
    waitForKernel();
    val kernel = launcher.getContext().getService(Kernel.class);
    while (kernel.getModuleManager().getModules().size() != count) {
      Thread.sleep(100);
    }
  }

  protected void waitForKernel() {
    if (launcher == null) {
      throw new IllegalStateException("You must call doRun() first");
    }

    for (; ; ) {
      val kernel = launcher.getContext().getService(Kernel.class);
      if (kernel == null) {
        continue;
      }
      val lifecycle = kernel.getLifecycle();
      if (lifecycle.getState() == KernelLifecycle.State.Running) {
        break;
      }
    }
  }
}
