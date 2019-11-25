package io.sunshower.kernel.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.sunshower.kernel.test.Module;
import io.sunshower.kernel.test.Modules;
import io.sunshower.kernel.test.ZephyrTest;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.core.ModuleManager;
import javax.inject.Inject;
import org.junit.jupiter.api.Test;

@ZephyrTest
@Modules({
  @Module(project = "kernel-tests:test-plugins:test-plugin-spring"),
  @Module(project = "kernel-modules:sunshower-yaml-reader", type = Module.Type.KernelModule)
})
class KernelRegistryMementoSystemTest {

  @Inject private Kernel kernel;

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
  void ensureSinglePluginIsInstalledAtBoot() {
    assertEquals(moduleManager.getModules().size(), 1);
  }
}
