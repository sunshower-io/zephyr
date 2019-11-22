package io.zephyr.kernel.core;

import io.sunshower.test.common.Tests;
import io.zephyr.kernel.Lifecycle;
import io.zephyr.kernel.launch.CommandTestCase;
import io.zephyr.kernel.launch.KernelLauncher;
import io.zephyr.kernel.server.Server;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;
import java.nio.file.FileSystems;
import java.util.UUID;

/**
 * I guess these tests should live by the module memento, but that introduces dependency weirdness
 */
class DefaultModuleManagerMementoTest extends CommandTestCase {

  private File testPlugin1;
  private File testPlugin2;
  private File yamlplugin;
  private File springplugin;
  private Server server;

  @BeforeEach
  void setUp() {

    retrievePlugins();
    server = startServer();
    installPlugins();
  }

  @AfterEach
  protected void tearDown() {
    super.tearDown();
    KernelLauncher.main(new String[] {"kernel", "stop"});
    waitForKernelState(KernelLifecycle.State.Stopped);
    server.stop();
    try {
      FileSystems.getFileSystem(
              URI.create("droplet://io.zephyr.sunshower-yaml-reader?version=1.0.0-SNAPSHOT"))
          .close();
    } catch (Exception ex) {

    }
  }

  @Test
  @RepeatedTest(10)
  void ensureInstallingAndStartingSpringWorksAfterKernelRestart() {}

  private void installPlugins() {

    KernelLauncher.main(
        new String[] {
          "kernel", "start", "-h", Tests.createTemp("test-2" + UUID.randomUUID()).getAbsolutePath()
        });
    waitForKernel();
    runRemote(
        "plugin",
        "install",
        testPlugin1.getAbsolutePath(),
        testPlugin2.getAbsolutePath(),
        yamlplugin.getAbsolutePath());

    waitForPluginState(
        t ->
            t.stream().filter(u -> u.getLifecycle().getState() == Lifecycle.State.Resolved).count()
                == 2);
    //    runRemote("kernel", "restart");
    //    waitForKernelState(KernelLifecycle.State.Stopped);
    //    waitForKernel();
  }

  private void retrievePlugins() {
    testPlugin1 =
        Tests.relativeToProjectBuild("kernel-tests:test-plugins:test-plugin-1", "war", "libs");
    testPlugin2 =
        Tests.relativeToProjectBuild("kernel-tests:test-plugins:test-plugin-2", "war", "libs");
    yamlplugin =
        Tests.relativeToProjectBuild("kernel-modules:sunshower-yaml-reader", "war", "libs");
    springplugin =
        Tests.relativeToProjectBuild("kernel-tests:test-plugins:test-plugin-spring", "war", "libs");
  }
}
