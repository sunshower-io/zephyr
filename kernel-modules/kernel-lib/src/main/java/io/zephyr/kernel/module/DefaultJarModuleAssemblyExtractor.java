package io.zephyr.kernel.module;

import java.util.Collections;
import java.util.Set;

public class DefaultJarModuleAssemblyExtractor extends JarModuleAssemblyExtractor {
  public DefaultJarModuleAssemblyExtractor() {
    super(Collections.singleton("lib"), Set.of("META-INF", "META-INF/MANIFEST.MF"));
  }
}
