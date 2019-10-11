package io.sunshower.kernel.modules;

import static org.jboss.modules.ResourceLoaderSpec.createResourceLoaderSpec;

import java.io.File;
import java.util.jar.JarFile;
import lombok.AllArgsConstructor;
import lombok.val;
import org.jboss.modules.*;

@AllArgsConstructor
public class ModuleClasspathFinder implements ModuleFinder {

  final File file;

  @Override
  public ModuleSpec findModule(String name, ModuleLoader delegateLoader)
      throws ModuleLoadException {
    try {
      val spec = ModuleSpec.build("test");
      val jarFile = new JarFile(file);

      addRootSpec(spec, jarFile);
      addClassesSpec(spec, jarFile);

      addSubresourceSpecs(spec, jarFile);

      spec.addDependency(DependencySpec.OWN_DEPENDENCY);
      return spec.create();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }

  private void addSubresourceSpecs(ModuleSpec.Builder spec, JarFile jarFile) {

    //    val iter = jarFile.entries();
    //    while (iter.hasMoreElements()) {
    //      if (iter.hasMoreElements()) {
    //        System.out.println(iter.nextElement());
    //      }
    //    }
  }

  private void addRootSpec(ModuleSpec.Builder spec, JarFile file) {
    val rootLoader = new ExtensionFileResourceLoader("test", file);
    val rootSpec = createResourceLoaderSpec(rootLoader);
    spec.addResourceRoot(rootSpec);
  }

  private void addClassesSpec(ModuleSpec.Builder spec, JarFile file) {
    val rootLoader = new ExtensionFileResourceLoader("test", file, "WEB-INF/classes");
    val rootSpec = createResourceLoaderSpec(rootLoader);
    spec.addResourceRoot(rootSpec);
  }
}
