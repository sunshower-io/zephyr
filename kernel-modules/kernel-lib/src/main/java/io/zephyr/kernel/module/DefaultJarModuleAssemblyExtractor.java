package io.zephyr.kernel.module;

import java.util.Collections;

public class DefaultJarModuleAssemblyExtractor extends JarModuleAssemblyExtractor {
  public DefaultJarModuleAssemblyExtractor() {
    super(Collections.singleton("lib"), Collections.singleton("META-INF"));
  }
}
