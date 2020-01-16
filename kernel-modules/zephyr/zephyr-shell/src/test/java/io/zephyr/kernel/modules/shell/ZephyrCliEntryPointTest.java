package io.zephyr.kernel.modules.shell;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

class ZephyrCliEntryPointTest extends ShellTestCase {

  @RepeatedTest(2)
  void ensureServerLifecycleIsIdempotent() throws InterruptedException {
    startServer();
    stopServer();
  }

  @Test
  void ensureStartingServerWorks() {
    startServer();
    assertTrue(server.isRunning(), "server must be running");
    stopServer();
    assertFalse(server.isRunning(), "server must not be running");
  }

  @RepeatedTest(2)
  void ensureStartingAndStoppingKernelWorks() {
    startServer();
    startKernel();
    stopKernel();
    stopServer();
  }

  @RepeatedTest(5)
  void ensureInstallingKernelModuleWorks() throws InterruptedException {
    try {
      startServer();
      startKernel();
      install(StandardModules.YAML);
      restartKernel();
      assertEquals(1, kernel.getKernelModules().size(), "must only have one kernel module");
    } finally {
      stopKernel();
      stopServer();
    }
  }
}
