package io.zephyr.platform.api;

import java.io.File;
import java.nio.file.FileSystem;

public interface NativeBinaryLifecycleManager {
  File extract(FileSystem moduleFilesystem);
}
