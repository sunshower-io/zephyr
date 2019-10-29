package io.sunshower.kernel.fs;

import io.sunshower.kernel.misc.SuppressFBWarnings;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collections;
import java.util.Set;
import lombok.NonNull;

public class ModuleFileSystem extends FileSystem implements Closeable {

  public static final String SUNSHOWER_HOME = "sunshower::filesystem::home";

  final String[] key;
  final File rootDirectory;
  final Path rootDirectoryPath;
  final ModuleFileSystemProvider fileSystemProvider;

  @SuppressFBWarnings
  @SuppressWarnings("PMD.ArrayIsStoredDirectly")
  public ModuleFileSystem(
      @NonNull String[] key,
      @NonNull ModuleFileSystemProvider provider,
      @NonNull File rootDirectory) {
    this.key = key;
    this.rootDirectory = rootDirectory;
    this.fileSystemProvider = provider;
    this.rootDirectoryPath = rootDirectory.toPath();
  }

  @Override
  public FileSystemProvider provider() {
    return fileSystemProvider;
  }

  @Override
  public void close() throws IOException {
    fileSystemProvider.closeFileSystem(this);
  }

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
    return Collections.singletonList(rootDirectoryPath);
  }

  @Override
  public Iterable<FileStore> getFileStores() {
    return null;
  }

  @Override
  public Set<String> supportedFileAttributeViews() {
    return null;
  }

  @Override
  @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
  public Path getPath(@NonNull String first, @NonNull String... more) {
    var path = new File(rootDirectory, first).toPath().toAbsolutePath();
    for (String p : more) {
      path = path.resolve(p);
    }
    return path;
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
