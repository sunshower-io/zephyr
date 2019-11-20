package io.zephyr.kernel.launch;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.sunshower.test.common.Tests;
import io.zephyr.kernel.core.SunshowerKernel;
import io.zephyr.kernel.misc.SuppressFBWarnings;
import io.zephyr.kernel.server.Server;
import java.net.URI;
import java.nio.file.FileSystems;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
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

  @AfterEach
  @SuppressFBWarnings
  @SuppressWarnings("PMD.EmptyCatchBlock")
  void tearDown() {
    try {
      val options = new KernelOptions();
      options.setHomeDirectory(Tests.createTemp());
      SunshowerKernel.setKernelOptions(options);
      val fs = FileSystems.getFileSystem(URI.create("droplet://kernel"));
      fs.close();
    } catch (Exception ex) {
      // eh
    }
  }
}
