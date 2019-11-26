package io.sunshower.kernel.core;

import io.sunshower.kernel.test.Module;
import io.sunshower.kernel.test.Modules;
import io.sunshower.kernel.test.ZephyrTest;
import io.sunshower.test.common.Tests;
import io.zephyr.api.Zephyr;
import io.zephyr.kernel.Lifecycle;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.core.ModuleManager;
import java.nio.file.Files;
import javax.inject.Inject;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.*;

@ZephyrTest
@Modules({
  @Module(project = "kernel-tests:test-plugins:test-plugin-spring"),
  @Module(project = "kernel-modules:sunshower-yaml-reader", type = Module.Type.KernelModule)
})
class KernelRegistryMementoSystemTest {

  @Inject private Kernel kernel;
  @Inject private Zephyr zephyr;

  @Inject private ModuleManager moduleManager;

  @Test
  void ensureYamlModuleIsInstalled() throws ClassNotFoundException {
    assertNotNull(
        Class.forName(
            "io.sunshower.kernel.ext.scanner.YamlPluginDescriptorScanner",
            true,
            kernel.getClassLoader()));
  }

  @Test
  void ensureComplexScenarioWithDependenciesWorks() throws Exception {
    val pluginDep =
        Tests.relativeToProjectBuild(
            "kernel-tests:test-plugins:test-plugin-spring-dep", "war", "libs");
    zephyr.install(pluginDep.toURI().toURL());
    zephyr.start(
        "io.sunshower.spring:spring-plugin:1.0.0", "io.sunshower.spring:spring-plugin-dep:1.0.0");

    assertEquals(
        moduleManager.getModules(Lifecycle.State.Active).size(), 2, "must have 2 active modules");

    zephyr.shutdown();

    assertEquals(
        moduleManager.getModules(Lifecycle.State.Active).size(),
        0,
        "must have zero active modules");
    zephyr.startup();
    System.out.println(moduleManager.getModules(Lifecycle.State.Active));
    assertEquals(
        2, moduleManager.getModules(Lifecycle.State.Active).size(), "must have 2 active modules");
  }

  @Test
  void ensurePluginIsRestartedWhenKernelIsRestarted() throws Exception {
    zephyr.start("io.sunshower.spring:spring-plugin:1.0.0");
    assertEquals(
        moduleManager.getModules(Lifecycle.State.Active).size(), 1, "must have one active plugin");

    kernel.persistState().toCompletableFuture().get();
    kernel.stop();
    assertTrue(
        moduleManager.getModules(Lifecycle.State.Active).isEmpty(), "kernel was stopped jfc");
    kernel.start();
    kernel.restoreState().toCompletableFuture().get();
    assertEquals(
        moduleManager.getModules(Lifecycle.State.Active).size(), 1, "kernel was started jfc");
  }

  @Test
  void ensureSinglePluginIsInstalledAtBoot() {
    assertEquals(moduleManager.getModules().size(), 1);
  }

  @Test
  void ensureModuleCanBeWrittenOutCorrectly() throws Exception {
    val module = moduleManager.getModules().get(0);
    val memento = module.save();
    val file = module.getFileSystem().getPath("plugin.yaml");
    try (val fwriter = Files.newOutputStream(file)) {
      memento.write(fwriter);
    }
  }
}
