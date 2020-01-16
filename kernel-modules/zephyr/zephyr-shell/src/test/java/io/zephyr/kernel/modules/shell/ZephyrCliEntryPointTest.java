package io.zephyr.kernel.modules.shell;

import static org.junit.jupiter.api.Assertions.*;

import io.sunshower.test.common.Tests;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

class ZephyrCliEntryPointTest extends ShellTestCase {

  @BeforeEach
  void setUp() {
    homeDirectory = Tests.createTemp();
  }

  @AfterEach
  void tearDown() {}

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

  @Test
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
