package io.zephyr.kernel.modules.shell;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ZephyrCliEntryPointTest extends ShellTestCase {

  @RepeatedTest(10)
  void ensureServerLifecycleIsIdempotent() throws InterruptedException {
    startServer();
    stopServer();
  }

  @RepeatedTest(10)
  void ensureStartingServerWorks() {
    startServer();
    assertTrue(server.isRunning(), "server must be running");
    stopServer();
    assertFalse(server.isRunning(), "server must not be running");
  }

  @Test
  void ensureStartingAndStoppingKernelWorks() {
    startKernel();
    stopKernel();
    stopServer();
  }

  @Test
  void ensureInstallingKernelModuleWorks() {
    startKernel();
    install(StandardModules.YAML);
    restartKernel();
    assertEquals(1, kernel.getKernelModules().size(), "must have 1 module");
    stopKernel();
    stopServer();
  }
}
