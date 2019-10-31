package io.sunshower.kernel.fs;

import static io.sunshower.kernel.core.SunshowerKernel.getKernelOptions;
import static java.lang.String.format;

import io.sunshower.common.io.FilePermissionChecker;
import io.sunshower.common.io.Files;
import io.sunshower.kernel.log.Logger;
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
import java.util.*;
import java.util.regex.Pattern;
import lombok.NonNull;
import lombok.val;

public class ModuleFileSystemProvider extends FileSystemProvider implements Closeable {

  static final Pattern queryPattern = Pattern.compile("=");
  static final Pattern keyPattern = Pattern.compile("\\.");
  static final Logger log = Logging.get(ModuleFileSystemProvider.class, "FileSystem");
  /** external state */
  static final String SCHEME = "droplet";

  public static final String VERSION = "version";
  public static final int QUERY_STRING_LENGTH = 2;

  private static FileSystemRegistry registry;

  static {
    registry = new FileSystemRegistry();
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
    val segments = computeSegments(uri);
    if (registry.contains(segments)) {
      throw new FileSystemAlreadyExistsException(host);
    }
    val result = new ModuleFileSystem(segments, this, doCreateDirectory(Files.toPath(segments)));
    registry.add(segments, result);
    return result;
  }

  private String[] computeSegments(URI uri) throws FileSystemException {
    if (uri.getQuery() == null) {
      return keyPattern.split(uri.getHost());
    } else {
      val hostParts = new ArrayList<>(Arrays.asList(keyPattern.split(uri.getHost())));
      hostParts.addAll(parseVersion(uri));
      return hostParts.toArray(new String[0]);
    }
  }

  @Override
  public FileSystem getFileSystem(URI uri) {
    val host = uri.getHost();
    if (host == null) {
      throw new FileSystemNotFoundException();
    }
    val result = registry.get(host);
    if (result == null) {
      throw new FileSystemNotFoundException(host);
    }
    return result;
  }

  @Override
  @SuppressWarnings({"PMD.DataflowAnomalyAnalysis", "PMD.AvoidLiteralsInIfCondition"})
  public Path getPath(@NonNull URI uri) {
    val fs = getFileSystem(uri);
    val path = uri.getPath();
    val segments = path.split(Files.separator);

    if (path.startsWith(File.separator)) {
      if (segments.length > 1) {
        val pathSegs = Arrays.copyOfRange(segments, 2, segments.length);
        return fs.getPath(segments[1], pathSegs);
      }
    }
    throw new IllegalArgumentException(
        format("Path '%s' was not understood by this filesystem", uri));
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
    registry.remove(system.key);
  }

  @Override
  public void setAttribute(Path path, String attribute, Object value, LinkOption... options)
      throws IOException {}

  @Override
  public void close() throws IOException {
    for (val fs : registry) {
      closeFileSystem((ModuleFileSystem) fs);
    }
  }

  private Collection<? extends String> parseVersion(URI uri) throws FileSystemException {
    val query = uri.getQuery();
    val parts = queryPattern.split(query);
    if (parts.length != QUERY_STRING_LENGTH) {
      throw new FileSystemException(
          format(
              "Failed to create filesystem.  Version '%s' isn't valid.  Expected 'version=<version>'",
              query));
    }
    if (!VERSION.equals(parts[0])) {
      throw new FileSystemException(
          format(
              "Failed to create filesystem.  Version '%s' isn't valid.  Expected 'version=<version>'",
              query));
    }
    return Collections.singletonList(parts[1]);
  }

  private File doCreateDirectory(Path toPath) throws FileSystemException {
    val result = fileSystemRoot.toPath().resolve(toPath).toAbsolutePath().toFile();
    if (!(result.exists() || result.mkdirs())) {
      throw new FileSystemException("Failed to create module directory: " + result);
    }
    return result;
  }
}
