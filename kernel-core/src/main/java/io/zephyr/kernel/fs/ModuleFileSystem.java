package io.zephyr.kernel.fs;

import io.zephyr.kernel.misc.SuppressFBWarnings;
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
    return FileSystems.getDefault().isOpen();
  }

  @Override
  public boolean isReadOnly() {
    return FileSystems.getDefault().isReadOnly();
  }

  @Override
  public String getSeparator() {
    return File.separator;
  }

  @Override
  public Iterable<Path> getRootDirectories() {
    return Collections.singletonList(rootDirectoryPath);
  }

  @Override
  public Iterable<FileStore> getFileStores() {
    return FileSystems.getDefault().getFileStores();
  }

  @Override
  public Set<String> supportedFileAttributeViews() {
    return FileSystems.getDefault().supportedFileAttributeViews();
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
    return FileSystems.getDefault().getPathMatcher(syntaxAndPattern);
  }

  @Override
  public UserPrincipalLookupService getUserPrincipalLookupService() {
    return FileSystems.getDefault().getUserPrincipalLookupService();
  }

  @Override
  public WatchService newWatchService() throws IOException {
    return FileSystems.getDefault().newWatchService();
  }
}
