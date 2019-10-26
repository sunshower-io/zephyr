package io.sunshower.common.io;

import io.sunshower.kernel.misc.SuppressFBWarnings;
import java.io.*;
import java.net.URL;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
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
}
