package io.sunshower.module.phases;

import static io.sunshower.kernel.Tests.resolve;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import io.sunshower.kernel.Module;
import io.sunshower.kernel.Tests;
import io.sunshower.kernel.core.KernelModuleLoader;
import io.sunshower.kernel.misc.SuppressFBWarnings;
import java.io.IOException;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings({
  "PMD.JUnitAssertionsShouldIncludeMessage",
  "PMD.JUnitTestsShouldIncludeAssert",
  "PMD.UseProperClassLoader"
})
@SuppressFBWarnings
public class ModuleClassloaderModuleTest extends AbstractModulePhaseTestCase {

  private Module module;
  private String moduleId;
  private KernelModuleLoader contextLoader;
  private Tests.InstallationContext installationContext;
  private org.jboss.modules.Module moduleClasspath;

  @Override
  @BeforeEach
  void setUp() throws Exception {
    super.setUp();
    installationContext = resolve("test-plugin-1", context);

    module = installationContext.getInstalledModule();
    moduleId = module.getCoordinate().toCanonicalForm();

    contextLoader = new KernelModuleLoader(dependencyGraph);

    contextLoader.install(installationContext.getInstalledModule());
    moduleClasspath = contextLoader.loadModule(moduleId);
  }

  @Test
  void ensureResolvingModuleResultsInModuleBeingResolved() throws Exception {
    Class.forName("plugin1.Test", true, moduleClasspath.getClassLoader());
  }

  @Test
  void ensureLoadingManifestWorks() {
    assertNotNull(moduleClasspath.getClassLoader().getResource("META-INF/MANIFEST.MF"));
  }

  @Test
  void ensureLoadingFromLibraryWorks() throws ClassNotFoundException {
    Class.forName(
        "com.esotericsoftware.yamlbeans.YamlWriter", true, moduleClasspath.getClassLoader());
  }

  @Test
  void ensureLoadingFromLibraryManifestWorks() {
    assertNotNull(
        moduleClasspath
            .getClassLoader()
            .getResource("META-INF/services/io.sunshower.kernel.ext.PluginDescriptorReader"));
  }

  @Test
  void ensureLoadingFromModuleDependencyWorks() throws Exception {

    dependencyGraph.add(installationContext.getInstalledModule());
    val ic = resolve("test-plugin-2", context);
    val imod = ic.getInstalledModule();
    dependencyGraph.add(imod);
    contextLoader.install(imod);

    try {
      val cl = contextLoader.loadModule(imod.getCoordinate().toCanonicalForm()).getClassLoader();
      Class.forName("plugin1.Test", true, cl);
    } finally {
      ic.getInstalledModule().getFileSystem().close();
    }
  }

  @Test
  void ensureUnloadingModuleWorks() throws Exception {
    dependencyGraph.add(installationContext.getInstalledModule());

    val ic = resolve("test-plugin-2", context);
    val imod = ic.getInstalledModule();
    dependencyGraph.add(imod);
    contextLoader.install(installationContext.getInstalledModule());
    contextLoader.install(imod);

    try {
      var cl = contextLoader.loadModule(imod.getCoordinate().toCanonicalForm()).getClassLoader();
      try {
        val clazz = Class.forName("plugin1.Test", true, cl);
        val obj = clazz.getConstructor().newInstance();
        assertNotNull(obj);
      } catch (Exception ex) {
        fail("Should not have reached here");
      }

      contextLoader.uninstall(installationContext.getInstalledModule());
    } finally {
      installationContext.getInstalledModule().getFileSystem().close();
      ic.getInstalledModule().getFileSystem().close();
    }
  }

  @Override
  @AfterEach
  void tearDown() throws IOException {
    super.tearDown();
    installationContext.getInstalledModule().getFileSystem().close();
  }
}
