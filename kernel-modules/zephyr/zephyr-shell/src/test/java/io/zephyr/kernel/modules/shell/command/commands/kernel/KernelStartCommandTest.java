package io.zephyr.kernel.modules.shell.command.commands.kernel;

import static org.junit.jupiter.api.Assertions.*;

import io.sunshower.test.common.Tests;
import io.zephyr.kernel.core.KernelLifecycle;
import io.zephyr.kernel.modules.shell.ShellTestCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class KernelStartCommandTest extends ShellTestCase {

  @BeforeEach
  protected void setUp() {
    homeDirectory = Tests.createTemp();
  }

  @AfterEach
  protected void tearDown() {}

  @Test
  void ensureStartingKernelResultsInKernelStarting() {
    startServer();
    startKernel();
    assertTrue(kernel.getLifecycle().getState() == KernelLifecycle.State.Running);
    stopKernel();
    stopServer();
  }
}
