package io.zephyr.spring.ext;

import io.zephyr.kernel.module.JarModuleAssemblyExtractor;
import java.util.Set;

public class SpringModuleAssemblyExtractor extends JarModuleAssemblyExtractor {

  public SpringModuleAssemblyExtractor() {
    super(Set.of("BOOT-INF/lib"), Set.of("BOOT-INF/classes/", "BOOT-INF/classes/META-INF/"));
  }
}
