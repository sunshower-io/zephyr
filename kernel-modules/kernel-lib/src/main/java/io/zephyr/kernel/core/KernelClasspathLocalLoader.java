package io.zephyr.kernel.core;

import static io.zephyr.kernel.core.ModulePackageConstraintSet.canReexportPackage;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import lombok.extern.java.Log;
import lombok.val;
import org.jboss.modules.LocalLoader;
import org.jboss.modules.Resource;

@Log
public class KernelClasspathLocalLoader implements LocalLoader {

  //    final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
  final ClassLoader classLoader;

  public KernelClasspathLocalLoader(final Kernel kernel) {
    this.classLoader = kernel.getClassLoader();
  }

  @Override
  public Class<?> loadClassLocal(String name, boolean resolve) {
    try {
      val type = Class.forName(name, true, classLoader);
      if (canReexportPackage(type.getPackageName())) {
        return type;
      } else {
        log.log(
            Level.INFO,
            String.format(
                "Found, but not loading external class ('%s')--not in Zephyr public API", name));
        return null;
      }
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  @Override
  public Package loadPackageLocal(String name) {
    return classLoader.getDefinedPackage(name);
  }

  @Override
  public List<Resource> loadResourceLocal(String name) {
    return Collections.emptyList();
  }
}
