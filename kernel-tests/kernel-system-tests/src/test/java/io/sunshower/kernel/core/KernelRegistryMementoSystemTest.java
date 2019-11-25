package io.sunshower.kernel.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.sunshower.kernel.test.Module;
import io.sunshower.kernel.test.Modules;
import io.sunshower.kernel.test.ZephyrTest;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.core.ModuleManager;
import java.nio.file.Files;
import javax.inject.Inject;
import lombok.val;
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
