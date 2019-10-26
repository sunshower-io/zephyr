package io.sunshower.module.phases;

import static org.junit.Assert.assertNotNull;

import io.sunshower.kernel.Module;
import io.sunshower.kernel.dependencies.DependencyGraph;
import io.sunshower.kernel.lifecycle.ModuleContextLoader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ModuleClassloaderModuleTest extends AbstractModulePhaseTestCase {

  private Module module;
  private String moduleId;
  private ModuleContextLoader contextLoader;
  private DependencyGraph dependencyGraph;
  private InstallationContext installationContext;
  private org.jboss.modules.Module moduleClasspath;

  @BeforeEach
  void setUp() throws Exception {
    super.setUp();
    installationContext = resolve("test-plugin-1");

    module = installationContext.getInstalledModule();
    moduleId = module.getCoordinate().toCanonicalForm();
    dependencyGraph = DependencyGraph.create(Collections.singleton(module));
    contextLoader = new ModuleContextLoader(dependencyGraph);
    moduleClasspath = contextLoader.createModuleContext().loadModule(moduleId);
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
    val ic = resolve("test-plugin-2");
    val imod = ic.getInstalledModule();
    val depgraph = DependencyGraph.create(Arrays.asList(imod, module));
    val cl = new ModuleContextLoader(depgraph);
    val moduleContext = cl.createModuleContext();
    val moduleLoader = moduleContext.loadModule(imod.getCoordinate().toCanonicalForm());

    try {
      Class.forName("plugin1.Test", true, moduleLoader.getClassLoader());
    } finally {
      ic.getInstalledModule().getFileSystem().close();
    }
  }

  @AfterEach
  void tearDown() throws IOException {
    super.tearDown();
    installationContext.getInstalledModule().getFileSystem().close();
  }
}
