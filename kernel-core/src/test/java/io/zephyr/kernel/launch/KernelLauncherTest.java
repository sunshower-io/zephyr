package io.zephyr.kernel.launch;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.sunshower.test.common.Tests;
import io.zephyr.kernel.server.Server;
import org.junit.jupiter.api.RepeatedTest;

@SuppressWarnings({
  "PMD.DoNotUseThreads",
  "PMD.AvoidUsingVolatile",
  "PMD.AvoidDuplicateLiterals",
  "PMD.DataflowAnomalyAnalysis",
  "PMD.JUnitTestsShouldIncludeAssert",
  "PMD.JUnitTestContainsTooManyAsserts",
  "PMD.JUnitAssertionsShouldIncludeMessage"
})
class KernelLauncherTest extends CommandTestCase {

  @RepeatedTest(5)
  void ensureStartingServerWorks() {
    Server server = startServer();
    assertTrue(server.isRunning(), "server must be running");
    KernelLauncher.main(new String[] {"server", "stop"});
    assertFalse(server.isRunning(), "server must not be running");
  }

  @RepeatedTest(5)
  void ensureStartingKernelWorks() throws InterruptedException {
    startServer();
    KernelLauncher.main(
        new String[] {"kernel", "start", "-h", Tests.createTemp().getAbsolutePath()});

    waitForKernel();
    KernelLauncher.main(new String[] {"server", "stop"});
  }
}
