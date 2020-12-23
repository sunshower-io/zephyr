package io.zephyr.kernel.fs;

import java.io.File;
import java.nio.file.*;

final class ScopedModuleFileSystemProvider extends ModuleFileSystemProvider {
  final File rootDirectory;
  final ModuleFileSystem current;

  public ScopedModuleFileSystemProvider(final File rootDirectory, final ModuleFileSystem current)
      throws AccessDeniedException {
    this.current = current;
    this.rootDirectory = rootDirectory;
  }

  @Override
  protected Path resolve(Path other) {
    return current.rootDirectoryPath.resolve(other);
  }
}
