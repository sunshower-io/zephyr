package io.sunshower.kernel.modules;

import java.util.jar.JarEntry;

public interface ClassIndex {

  /**
   * @param className the binary name of the desired class
   * @return the class if it exists in this index, or null
   */
  JarEntry getEntry(String className);
}
