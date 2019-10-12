package io.sunshower.kernel.modules;

import static io.sunshower.kernel.KernelTests.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.sunshower.test.common.Tests;
import java.io.File;
import java.util.UUID;
import java.util.concurrent.Executors;
import lombok.SneakyThrows;
import lombok.val;
import org.jboss.modules.*;
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
  @SneakyThrows
  void ensureModuleLoadingWorksForSubModule() {
    val km = loadTestModuleFile("sunshower-yaml-reader", "war");
    val classIndex = index(km);
    val loader = new ModuleLoader(new ModuleClasspathFinder(km, classIndex));
    val module = loader.loadModule("test");
    Class.forName(
        "io.sunshower.kernel.modules.descriptors.PluginHolder", true, module.getClassLoader());
    Class c =
        Class.forName("com.esotericsoftware.yamlbeans.YamlConfig", true, module.getClassLoader());
    val instance = c.getConstructor().newInstance();
    assertEquals(instance.getClass().getClassLoader(), loader.loadModule("test").getClassLoader());
  }

  private ClassIndex index(File file) {
    val dir = Tests.createTemp(UUID.randomUUID().toString());
    val indexer = new OffHeapClassPathIndexer(file, dir, Executors.newScheduledThreadPool(5));
    return indexer.index(true);
  }
}
