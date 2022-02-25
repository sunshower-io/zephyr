package io.zephyr.kernel.extensions;

import io.zephyr.kernel.Assembly;
import java.nio.file.FileSystem;

/**
 * This kernel extension point allows kernel modules to customize which classes/resources are added
 * to the module assembly
 */
public interface ModuleAssemblyExtractor extends Comparable<ModuleAssemblyExtractor> {

  default int compareTo(ModuleAssemblyExtractor other) {
    return Integer.compare(order(), other.order());
  }

  int order();

  boolean appliesTo(Assembly assembly, FileSystem moduleFilesystem);

  void extract(Assembly assembly, FileSystem moduleFilesystem, ExtractionListener listener)
      throws Exception;

  interface ExtractionListener {
    void beforeEntryExtracted(String name, Object target);

    void afterEntryExtracted(String name, Object target);
  }
}
