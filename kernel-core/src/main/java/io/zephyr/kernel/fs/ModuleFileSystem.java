package io.zephyr.kernel.fs;

import io.sunshower.checks.SuppressFBWarnings;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collections;
import java.util.Set;
import lombok.NonNull;

@SuppressWarnings({"PMD.AvoidFieldNameMatchingMethodName", "PMD.AvoidUsingVolatile"})
public class ModuleFileSystem extends FileSystem implements Closeable {

  public static final String SUNSHOWER_HOME = "sunshower::filesystem::home";

  final String[] key;
  final File rootDirectory;
  final Path rootDirectoryPath;
  final ModuleFileSystemProvider fileSystemProvider;
  private final Object lock = new Object();

  private volatile boolean open;
  private volatile ScopedModuleFileSystemProvider provider;

  @SuppressFBWarnings
  @SuppressWarnings("PMD.ArrayIsStoredDirectly")
  public ModuleFileSystem(
      @NonNull String[] key,
      @NonNull ModuleFileSystemProvider provider,
      @NonNull File rootDirectory) {
    this.key = key;
    this.open = true;
    this.rootDirectory = rootDirectory;
    this.fileSystemProvider = provider;
    this.rootDirectoryPath = rootDirectory.toPath();
  }

  @Override
  @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
  public FileSystemProvider provider() {
    FileSystemProvider result = provider;
    if (result == null) {
      synchronized (lock) {
        result = provider;
        if (result == null) {
          try {
            result = provider = new ScopedModuleFileSystemProvider(rootDirectory, this);
          } catch (AccessDeniedException ex) {
            throw new FileSystemAccessDeniedException(ex);
          }
        }
      }
    }
    return result;
  }

  @Override
  public void close() throws IOException {
    synchronized (lock) {
      try {
        fileSystemProvider.closeFileSystem(this);
      } finally {
        open = false;
      }
    }
  }

  @Override
  public boolean isOpen() {
    return open;
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
