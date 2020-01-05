package io.zephyr.kernel.module;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipFile;

public class JarModuleAssemblyExtractor extends AbstractCompressedModuleAssemblyExtractor {

  private final Set<String> libraryDirectories;
  private final Set<String> resourceDirectories;

  public JarModuleAssemblyExtractor(
      final Collection<String> libraryDirectories, final Collection<String> resourceDirectories) {
    this.libraryDirectories = new HashSet<>(libraryDirectories);
    this.resourceDirectories = new HashSet<>(resourceDirectories);
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
