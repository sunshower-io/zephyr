package io.zephyr.kernel.module;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.zip.ZipFile;

public class JarModuleAssemblyExtractor extends AbstractCompressedModuleAssemblyExtractor {

  private final Set<String> libraryDirectories;
  private final Set<String> resourceDirectories;

  public JarModuleAssemblyExtractor(
      final Collection<String> libraryDirectories, final Collection<String> resourceDirectories) {
    this.libraryDirectories = new LinkedHashSet<>(libraryDirectories);
    this.resourceDirectories = new LinkedHashSet<>(resourceDirectories);
  }

  public int order() {
    return 200;
  }

  @Override
  protected Collection<String> getLibraryDirectories() {
    return libraryDirectories;
  }

  @Override
  protected boolean isResourceDirectory(String name, ZipFile file) {
    return resourceDirectories.contains(name);
  }
}
