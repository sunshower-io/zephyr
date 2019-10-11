package io.sunshower.kernel.modules;

import static io.sunshower.kernel.KernelTests.*;
import static org.junit.jupiter.api.Assertions.*;

import lombok.SneakyThrows;
import lombok.val;
import org.jboss.modules.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class DependencyAwareModuleTest {

  @Test
  @SneakyThrows
  void ensureModuleLoadingWorks() {
    val km = loadTestPluginFile("test-plugin-1", "jar");
    val loader = new ModuleLoader(new ModuleClasspathFinder(km));
    val module = loader.loadModule("test");
    Class.forName("plugin1.Test", true, module.getClassLoader());
  }

  @Test
  @SneakyThrows
  void ensureModuleLoadingWorksForWebINFClasses() {
    val km = loadTestPluginFile("test-plugin-2", "war");
    val loader = new ModuleLoader(new ModuleClasspathFinder(km));
    val module = loader.loadModule("test");
    Class.forName("testproject2.Test", true, module.getClassLoader());
  }

  @Test
  @Disabled
  @SneakyThrows
  void ensureModuleLoadingWorksForSubModule() {
    val km = loadTestModuleFile("sunshower-yaml-reader", "war");
    val loader = new ModuleLoader(new ModuleClasspathFinder(km));
    val module = loader.loadModule("test");
    Class.forName("com.esotericsoftware.yamlbeans.Beans", true, module.getClassLoader());
  }
}
