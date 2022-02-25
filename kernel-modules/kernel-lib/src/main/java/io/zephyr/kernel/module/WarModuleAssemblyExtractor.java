package io.zephyr.kernel.module;

import java.util.Set;

public class WarModuleAssemblyExtractor extends JarModuleAssemblyExtractor {

  public int order() {
    return 100;
  }

  public WarModuleAssemblyExtractor() {
    super(Set.of("WEB-INF/lib/"), Set.of("WEB-INF/classes/"));
  }
}
