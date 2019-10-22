package io.sunshower.kernel.fs;

import io.sunshower.kernel.log.Logging;
import lombok.val;
import org.jetbrains.annotations.NotNull;

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
import java.util.logging.Logger;

public class ModuleFileSystemProvider extends FileSystemProvider {

  static final Logger log = Logging.get(ModuleFileSystemProvider.class, "FileSystem");
  /** external state */
  public static final String SCHEME = "droplet";

  private static final String KERNEL_PATH = "kernel";

  private static final Map<String, FileSystem> fileSystems;

  static {
    fileSystems = new ConcurrentHashMap<>();
  }

  public ModuleFileSystemProvider() {
  }

  @Override
  public String getScheme() {
    return SCHEME;
  }

  @Override
  public FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
    return new ModuleFileSystem(this, null);
  }

  @Override
  public FileSystem getFileSystem(URI uri) {
    val path = uri.getPath();
    if ("/".equals(path)) {
      return getRootFileSystem();
    }
    val host = uri.getHost();

    if (KERNEL_PATH.equals(host)) {
      return getRootFileSystem();
    }

    return new ModuleFileSystem(this, null);
  }

  @NotNull
  @Override
  public Path getPath(@NotNull URI uri) {
    return null;
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

  @Override
  public void setAttribute(Path path, String attribute, Object value, LinkOption... options)
      throws IOException {}

  private FileSystem getRootFileSystem() {
    FileSystem fs = fileSystems.get(KERNEL_PATH);
    if (fs == null) {
      fs = new ModuleFileSystem(this, null);
      fileSystems.put(KERNEL_PATH, fs);
    }
    return fs;
  }
}
