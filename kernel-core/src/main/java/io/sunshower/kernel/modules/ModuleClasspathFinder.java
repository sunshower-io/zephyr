package io.sunshower.kernel.modules;

import static org.jboss.modules.ResourceLoaderSpec.createResourceLoaderSpec;

import java.io.File;
import java.util.jar.JarFile;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.val;
import org.jboss.modules.*;

public class ModuleClasspathFinder implements ModuleFinder {

  final File file;
  final ClassIndex index;


  public ModuleClasspathFinder(@NonNull File file) {
    this(file, null);
  }

  public ModuleClasspathFinder(@NonNull File file, ClassIndex index) {
    this.file = file;
    this.index = index;
  }

  @Override
  public ModuleSpec findModule(String name, ModuleLoader delegateLoader)
      throws ModuleLoadException {
    try {
      val spec = ModuleSpec.build("test");
      val jarFile = new JarFile(file);

      addRootSpec(spec, jarFile);
      addClassesSpec(spec, jarFile);

      spec.addDependency(DependencySpec.OWN_DEPENDENCY);
      return spec.create();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }


  private void addRootSpec(ModuleSpec.Builder spec, JarFile file) {
    val rootLoader = new ExtensionFileResourceLoader("test", file, index);
    val rootSpec = createResourceLoaderSpec(rootLoader);
    spec.addResourceRoot(rootSpec);
  }

  private void addClassesSpec(ModuleSpec.Builder spec, JarFile file) {
    val rootLoader = new ExtensionFileResourceLoader("test", file, "WEB-INF/classes", null);
    val rootSpec = createResourceLoaderSpec(rootLoader);
    spec.addResourceRoot(rootSpec);
  }
}
