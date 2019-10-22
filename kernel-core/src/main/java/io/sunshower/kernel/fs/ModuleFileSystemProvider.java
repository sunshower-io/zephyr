package io.sunshower.kernel.fs;

import static io.sunshower.kernel.core.SunshowerKernel.getKernelOptions;

import io.sunshower.common.io.FilePermissionChecker;
import io.sunshower.common.io.Files;
import io.sunshower.kernel.log.Logging;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.val;
import org.jetbrains.annotations.NotNull;

public class ModuleFileSystemProvider extends FileSystemProvider implements Closeable {

  static final Logger log = Logging.get(ModuleFileSystemProvider.class, "FileSystem");
  /** external state */
  static final String SCHEME = "droplet";

  private static final Map<String, FileSystem> fileSystems;

  static {
    fileSystems = new ConcurrentHashMap<>();
  }

  private final File fileSystemRoot;

  public ModuleFileSystemProvider() throws AccessDeniedException {
    val options = getKernelOptions();
    this.fileSystemRoot =
        Files.check(
            options.getHomeDirectory(),
            FilePermissionChecker.Type.READ,
            FilePermissionChecker.Type.WRITE);
  }

  @Override
  public String getScheme() {
    return SCHEME;
  }

  @Override
  public FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
    val host = uri.getHost();
    if (host == null || host.isBlank()) {
      throw new FileSystemException("Cannot create filesystem from null or blank host");
    }
    return fileSystems.compute(host, this::create);
  }

  @Override
  public FileSystem getFileSystem(URI uri) {
    val host = uri.getHost();
    if (!fileSystems.containsKey(host)) {
      throw new FileSystemNotFoundException(host);
    }
    return fileSystems.get(host);
  }

  @NotNull
  @Override
  public Path getPath(@NotNull URI uri) {
    val fs = getFileSystem(uri);
    return fs.getPath(uri.getPath());
  }

  @Override
  public SeekableByteChannel newByteChannel(
      Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
    return null;
  }

  @Override
  public DirectoryStream<Path> newDirectoryStream(
      Path dir, DirectoryStream.Filter<? super Path> filter) throws IOException {
    return null;
  }

  @Override
  public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {}

  @Override
  public void delete(Path path) throws IOException {}

  @Override
  public void copy(Path source, Path target, CopyOption... options) throws IOException {}

  @Override
  public void move(Path source, Path target, CopyOption... options) throws IOException {}

  @Override
  public boolean isSameFile(Path path, Path path2) throws IOException {
    return false;
  }

  @Override
  public boolean isHidden(Path path) throws IOException {
    return false;
  }

  @Override
  public FileStore getFileStore(Path path) throws IOException {
    return null;
  }

  @Override
  public void checkAccess(Path path, AccessMode... modes) throws IOException {}

  @Override
  public <V extends FileAttributeView> V getFileAttributeView(
      Path path, Class<V> type, LinkOption... options) {
    return null;
  }

  @Override
  public <A extends BasicFileAttributes> A readAttributes(
      Path path, Class<A> type, LinkOption... options) throws IOException {
    return null;
  }

  @Override
  public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options)
      throws IOException {
    return null;
  }

  protected void closeFileSystem(ModuleFileSystem system) throws IOException {
    fileSystems.remove(system.key);
  }

  @Override
  public void setAttribute(Path path, String attribute, Object value, LinkOption... options)
      throws IOException {}

  private FileSystem create(String host, FileSystem fileSystem) {
    if (fileSystem == null) {
      log.log(Level.FINE, "filesystem.new", host);
      val fs = new ModuleFileSystem(host, this, new File(fileSystemRoot, host));
      log.log(Level.FINE, "filesystem.new.success", host);
      return fs;
    } else {
      log.log(Level.WARNING, "filesystem.new.exists", host);

      throw new FileSystemAlreadyExistsException(host);
    }
  }

  @Override
  public void close() throws IOException {
    for (val fs : fileSystems.entrySet()) {
      closeFileSystem((ModuleFileSystem) fs.getValue());
    }
  }
}
