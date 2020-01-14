package command.commands.plugin;

// import io.sunshower.test.common.Tests;
// import io.zephyr.kernel.launch.CommandTestCase;
// import io.zephyr.kernel.launch.KernelLauncher;
// import lombok.val;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
//
// import java.io.File;
// import java.util.UUID;
//
// import static org.junit.jupiter.api.Assertions.assertEquals;

import launch.CommandTestCase;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class InstallPluginCommandTest extends CommandTestCase {
  //
  //  File testPlugin1;
  //  File testPlugin2;
  //
  //  @BeforeEach
  //  void setUp() {
  //
  //    testPlugin1 =
  //        Tests.relativeToProjectBuild("kernel-tests:test-plugins:test-plugin-1", "war", "libs");
  //    testPlugin2 =
  //        Tests.relativeToProjectBuild("kernel-tests:test-plugins:test-plugin-2", "war", "libs");
  //  }
  //
  //  @Test
  //  void ensureInstallingPluginWorks() throws InterruptedException {
  //    val server = startServer();
  //    try {
  //      KernelLauncher.main(
  //          new String[] {"kernel", "start", "-h", Tests.createTemp().getAbsolutePath()});
  //      waitForKernel();
  //      runRemote("plugin", "install", testPlugin1.getAbsolutePath());
  //      waitForPluginCount(1);
  //      val kernel = getKernel();
  //      assertEquals(
  //          kernel.getModuleManager().getModules().size(), 1, "must have one plugin installed");
  //    } finally {
  //      KernelLauncher.main(new String[] {"kernel", "stop"});
  //      server.stop();
  //    }
  //  }
  //
  //  @Test
  //  void ensureInstallingMultiplePluginsWorks() {
  //
  //    val server = startServer();
  //    try {
  //      KernelLauncher.main(
  //          new String[] {
  //            "kernel", "start", "-h", Tests.createTemp("hello" +
  // UUID.randomUUID()).getAbsolutePath()
  //          });
  //      waitForKernel();
  //      runRemote("plugin", "install", testPlugin1.getAbsolutePath(),
  // testPlugin2.getAbsolutePath());
  //      waitForPluginCount(2);
  //      val kernel = getKernel();
  //      assertEquals(
  //          kernel.getModuleManager().getModules().size(), 2, "must have two plugins installed");
  //    } finally {
  //      KernelLauncher.main(new String[] {"kernel", "stop"});
  //      server.stop();
  //    }
  //  }
}
