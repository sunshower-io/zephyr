package io.zephyr.common.io;

import io.zephyr.kernel.memento.Memento;
import io.zephyr.kernel.misc.SuppressFBWarnings;
import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import lombok.NonNull;
import lombok.val;

public class Files {

  public static final String separator = File.separatorChar == '\\' ? "\\\\" : File.separator;

  static final int BUFFER_SIZE = 8128;

  @SuppressFBWarnings
  @SuppressWarnings({"PMD.AvoidFileStream", "PMD.DataflowAnomalyAnalysis"})
  public static void transferTo(File destination, InputStream inputStream) throws IOException {
    try (val is = inputStream;
        val os = new FileOutputStream(destination)) {
      val buffer = new byte[BUFFER_SIZE];
      for (; ; ) {
        int read = is.read(buffer);
        if (read == -1) {
          return;
        }
        os.write(buffer);
      }
    }
  }
  // adapted from
  // https://stackoverflow.com/questions/7379469/filechannel-transferto-for-large-file-in-windows
  @SuppressWarnings({"PMD.AvoidFileStream", "PMD.DataflowAnomalyAnalysis"})
  public static void transferTo(File from, File to) throws IOException {
    try (val inChannel = new FileInputStream(from).getChannel();
        val outChannel = new FileOutputStream(to).getChannel(); ) {
      int maxCount = (64 * 1024 * 1024) - (32 * 1024);
      long size = inChannel.size();
      long position = 0;
      while (position < size) {
        position += inChannel.transferTo(position, maxCount, outChannel);
      }
    }
  }

  @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
  public static Path toPath(@NonNull String... segments) {
    Path path = Path.of(segments[0]);
    for (int i = 1; i < segments.length; i++) {
      path = path.resolve(segments[i]);
    }
    return path;
  }

  public static String getFileName(@NonNull URL url) {
    val file = url.getFile();
    return file.substring(file.lastIndexOf("/"));
  }

  public static File check(File file, FilePermissionChecker.Type... checks)
      throws AccessDeniedException {
    for (val check : checks) {
      if (!check.check(file)) {
        throw new AccessDeniedException(file.getAbsolutePath(), null, check.name());
      }
    }
    return file;
  }

  public static String getFileName(String name) {
    val sepidx = name.lastIndexOf(separator);
    if (sepidx > 0) {
      return name.substring(sepidx + 1);
    }
    return name;
  }

  public static Path doCheck(Path path) throws IOException {
    val file = path.toFile().getAbsoluteFile();
    if (!file.exists()) {
      val parent = file.getParentFile();
      if (!parent.exists()) {
        if (!parent.mkdirs()) {
          throw new IllegalStateException("Error: could not create " + parent.getAbsolutePath());
        }
      }
      if (!file.createNewFile()) {
        throw new IllegalStateException(
            "Error: could not create file at " + file.getAbsolutePath());
      }
    }
    io.zephyr.common.io.Files.check(
        file,
        FilePermissionChecker.Type.DELETE,
        FilePermissionChecker.Type.WRITE,
        FilePermissionChecker.Type.READ);
    return file.toPath();
  }

  /**
   * this doesn't really belong here, but whatevs
   *
   * @param path
   * @param memento
   * @throws Exception
   */
  public static void tryWrite(Path path, Memento memento) throws Exception {
    try (val output =
        java.nio.file.Files.newOutputStream(
            doCheck(path),
            StandardOpenOption.WRITE,
            StandardOpenOption.DSYNC,
            StandardOpenOption.TRUNCATE_EXISTING)) {
      memento.write(output);
      output.flush();
    }
  }

  public static void deleteTree(File directory) throws IOException {
    java.nio.file.Files.walkFileTree(
        directory.toPath(),
        new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
              throws IOException {
            java.nio.file.Files.delete(file);
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            java.nio.file.Files.delete(dir);
            return FileVisitResult.CONTINUE;
          }
        });
  }
}
