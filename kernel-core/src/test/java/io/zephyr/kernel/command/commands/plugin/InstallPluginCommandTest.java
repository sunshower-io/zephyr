package io.zephyr.kernel.command.commands.plugin;

import io.sunshower.test.common.Tests;
import io.zephyr.kernel.launch.CommandTestCase;
import io.zephyr.kernel.launch.KernelLauncher;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InstallPluginCommandTest extends CommandTestCase {

  File testPlugin1;
  File testPlugin2;

  @BeforeEach
  void setUp() {

    testPlugin1 =
        Tests.relativeToProjectBuild("kernel-tests:test-plugins:test-plugin-1", "war", "libs");
    testPlugin2 =
        Tests.relativeToProjectBuild("kernel-tests:test-plugins:test-plugin-2", "war", "libs");
  }

  @Test
  void ensureInstallingPluginWorks() throws InterruptedException {
    startServer();
    KernelLauncher.main(
        new String[] {"kernel", "start", "-h", Tests.createTemp().getAbsolutePath()});
    waitForKernel();
    runRemote("plugin", "install", testPlugin1.getAbsolutePath());
    waitForPluginCount(1);
    val kernel = getKernel();
    assertEquals(
        kernel.getModuleManager().getModules().size(), 1, "must have one plugin installed");
  }

  @Test
  void ensureInstallingMultiplePluginsWorks() {

    startServer();
    KernelLauncher.main(
        new String[] {"kernel", "start", "-h", Tests.createTemp().getAbsolutePath()});
    waitForKernel();
    runRemote("plugin", "install", testPlugin1.getAbsolutePath(), testPlugin2.getAbsolutePath());
    waitForPluginCount(2);
    val kernel = getKernel();
    assertEquals(
        kernel.getModuleManager().getModules().size(), 2, "must have two plugins installed");
  }
}
