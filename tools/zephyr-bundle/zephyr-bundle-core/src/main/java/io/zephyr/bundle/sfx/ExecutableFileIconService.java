package io.zephyr.bundle.sfx;

import java.io.File;
import java.util.Collection;

/** */
public interface ExecutableFileIconService extends PlatformSpecificService {
  void setIcons(File executable, File icoFile, Log log);

  void setIcons(File executable, Collection<File> iconFiles, Log log);
}
