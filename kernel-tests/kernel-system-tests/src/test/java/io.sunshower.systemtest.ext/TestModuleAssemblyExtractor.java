package io.sunshower.systemtest.ext;

import io.zephyr.kernel.module.JarModuleAssemblyExtractor;
import java.util.Set;

public class TestModuleAssemblyExtractor extends JarModuleAssemblyExtractor {

  public TestModuleAssemblyExtractor() {
    super(
        Set.of("WEB-INF/lib/", "BOOT-INF/lib"),
        Set.of("WEB-INF/classes/", "META-INF/", "BOOT-INF/classes/", "BOOT-INF/classes/META-INF/"));
  }
}
