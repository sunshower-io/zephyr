package io.zephyr.bundle.sfx;

import static java.lang.String.format;

import java.nio.file.Files;
import java.nio.file.Path;

public class IOUtilities {

  public static void checkDirectory(Path directory, Log log) {

    if (!Files.exists(directory)) {
      throw new IllegalArgumentException(
          format("required directory '%s' does not exist", directory));
    }

    if (!Files.isDirectory(directory)) {
      throw new IllegalArgumentException(
          format("'%s' was expected to be a directory, but is a file instead", directory));
    }

    if (!Files.isWritable(directory)) {
      throw new IllegalArgumentException(
          format("required directory '%s' exists but is not writable ", directory));
    }

    if (!Files.isReadable(directory)) {
      throw new IllegalArgumentException(
          format("required directory '%s' exists but is not readable", directory));
    }
  }

  public static void checkFile(Path file, Log log) {
    if (!Files.exists(file)) {
      throw new IllegalArgumentException(format("required file '%s' does not exist", file));
    }

    if (Files.isDirectory(file)) {
      throw new IllegalArgumentException(
          format("file '%s' was expected to be a file, but is a directory instead", file));
    }

    if (!Files.isReadable(file)) {
      throw new IllegalArgumentException(
          format("required file '%s' exists but is not readable", file));
    }
  }
}
