package io.sunshower.kernel.fs;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Set;

/**
 * A module file system specifies a versioned filesystem for Sunshower kernel modules.  This entails:
 *
 *
 */
public class ModuleFileSystem extends FileSystem {

  public static final String SUNSHOWER_HOME = "sunshower::filesystem::home";


  final transient File rootDirectory;
  final transient FileSystemProvider fileSystemProvider;

  public ModuleFileSystem(@NonNull ModuleFileSystemProvider provider, File rootDirectory) {
    this.rootDirectory = rootDirectory;
    this.fileSystemProvider = provider;
  }

  @Override
  public FileSystemProvider provider() {
    return fileSystemProvider;
  }

  @Override
  public void close() throws IOException {}

  @Override
  public boolean isOpen() {
    return false;
  }

  @Override
  public boolean isReadOnly() {
    return false;
  }

  @Override
  public String getSeparator() {
    return null;
  }

  @Override
  public Iterable<Path> getRootDirectories() {
    return null;
  }

  @Override
  public Iterable<FileStore> getFileStores() {
    return null;
  }

  @Override
  public Set<String> supportedFileAttributeViews() {
    return null;
  }

  @NotNull
  @Override
  public Path getPath(@NotNull String first, @NotNull String... more) {
    return null;
  }

  @Override
  public PathMatcher getPathMatcher(String syntaxAndPattern) {
    return null;
  }

  @Override
  public UserPrincipalLookupService getUserPrincipalLookupService() {
    return null;
  }

  @Override
  public WatchService newWatchService() throws IOException {
    return null;
  }
}
