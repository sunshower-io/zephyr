package io.zephyr.kernel.modules.shell;

import io.sunshower.test.common.Tests;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.extensions.EntryPoint;
import io.zephyr.kernel.launch.KernelLauncher;
import io.zephyr.kernel.modules.shell.server.Server;
import launch.CommandTestCase;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

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
}
