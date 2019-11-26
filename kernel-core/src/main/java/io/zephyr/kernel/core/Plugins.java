package io.zephyr.kernel.core;

import io.sunshower.gyre.Pair;
import io.zephyr.kernel.Coordinate;
import lombok.val;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.util.Collections;

public class Plugins {

  static final String FILE_SYSTEM_URI_TEMPLATE = "droplet://%s.%s?version=%s";

  public static final Pair<String, FileSystem> getFileSystem(Coordinate coordinate)
      throws IOException {
    val uri =
        String.format(
            FILE_SYSTEM_URI_TEMPLATE,
            coordinate.getGroup(),
            coordinate.getName(),
            coordinate.getVersion());
    FileSystem fs;
    try {
      fs = FileSystems.getFileSystem(URI.create(uri));
    } catch (FileSystemNotFoundException ex) {
      fs = FileSystems.newFileSystem(URI.create(uri), Collections.emptyMap());
    }
    return Pair.of(uri, fs);
  }
}
