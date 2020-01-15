package command.commands.plugin;

import static org.junit.jupiter.api.Assertions.*;

import io.sunshower.test.common.Tests;
import io.zephyr.kernel.launch.KernelLauncher;
import java.io.File;
import java.util.UUID;
import launch.CommandTestCase;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class InstallPluginCommandTest extends CommandTestCase {

  File testPlugin1;
  File testPlugin2;
  File yamlModule;

  @BeforeEach
  void setUp() {
    yamlModule =
        Tests.relativeToProjectBuild("kernel-modules:sunshower-yaml-reader", "war", "libs");

    testPlugin1 =
        Tests.relativeToProjectBuild("kernel-tests:test-plugins:test-plugin-1", "war", "libs");
    testPlugin2 =
        Tests.relativeToProjectBuild("kernel-tests:test-plugins:test-plugin-2", "war", "libs");
  }

  @Test
  void ensureStartingServerWorks() {
    val server = startServer();
    assertTrue(server.isRunning(), "server must be started");
    server.stop();
    assertFalse(server.isRunning(), "server must be stopped");
  }

  @Test
  void ensureStartingKernelWorks() {
    KernelLauncher.main(
        new String[] {"kernel", "start", "-h", Tests.createTemp().getAbsolutePath()});
  }

  @Test
  void ensureInstallingKernelModuleWorks() {
    val server = startServer();
    try {
      KernelLauncher.main(
          new String[] {"kernel", "start", "-h", Tests.createTemp().getAbsolutePath()});
      waitForKernel();
      runKernel("plugin", "install", yamlModule.getAbsolutePath());
      //      waitForPluginCount(1);
      val kernel = getKernel();
      assertEquals(
          kernel.getModuleManager().getModules().size(), 1, "must have one plugin installed");
    } finally {
      KernelLauncher.main(new String[] {"kernel", "stop"});
      server.stop();
    }
  }

  @Test
  void ensureInstallingPluginWorks() throws InterruptedException {
    val server = startServer();
    try {
      KernelLauncher.main(
          new String[] {"kernel", "start", "-h", Tests.createTemp().getAbsolutePath()});
      waitForKernel();
      runKernel("plugin", "install", testPlugin1.getAbsolutePath());
      waitForPluginCount(1);
      val kernel = getKernel();
      assertEquals(
          kernel.getModuleManager().getModules().size(), 1, "must have one plugin installed");
    } finally {
      KernelLauncher.main(new String[] {"kernel", "stop"});
      server.stop();
    }
  }

  @Test
  void ensureInstallingMultiplePluginsWorks() {

    val dir = Tests.createTemp(UUID.randomUUID().toString()).getAbsolutePath();
    KernelLauncher.main(new String[] {"kernel", "start", "-h", dir});

    KernelLauncher.main(new String[] {"plugin", "install", dir, testPlugin1.getAbsolutePath()});
  }
}
