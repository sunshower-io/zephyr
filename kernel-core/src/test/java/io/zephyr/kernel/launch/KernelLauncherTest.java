package io.zephyr.kernel.launch;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.sunshower.test.common.Tests;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.core.KernelLifecycle;
import io.zephyr.kernel.server.Server;
import lombok.val;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

@SuppressWarnings({
  "PMD.DoNotUseThreads",
  "PMD.AvoidUsingVolatile",
  "PMD.AvoidDuplicateLiterals",
  "PMD.DataflowAnomalyAnalysis",
  "PMD.JUnitTestsShouldIncludeAssert",
  "PMD.JUnitTestContainsTooManyAsserts",
  "PMD.JUnitAssertionsShouldIncludeMessage"
})
class KernelLauncherTest {

  volatile KernelLauncher launcher;

  @RepeatedTest(5)
  void ensureStartingServerWorks() {
    Server server = startServer();
    assertTrue(server.isRunning(), "server must be running");
    KernelLauncher.main(new String[] {"server", "stop"});
    assertFalse(server.isRunning(), "server must not be running");
  }

  @Test
  @RepeatedTest(5)
  void ensureStartingKernelWorks() throws InterruptedException {
    startServer();
    KernelLauncher.main(
        new String[] {"kernel", "start", "-h", Tests.createTemp().getAbsolutePath()});

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
    KernelLauncher.main(new String[] {"server", "stop"});
  }

  private Server startServer() {
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

  private void doRun(String... args) {
    launcher = KernelLauncher.prepare(args);
    val thread = new Thread(() -> launcher.run());
    thread.start();
  }
}
