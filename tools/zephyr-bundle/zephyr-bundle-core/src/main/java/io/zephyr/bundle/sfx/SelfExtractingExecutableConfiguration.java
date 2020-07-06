package io.zephyr.bundle.sfx;

import java.io.File;

public interface SelfExtractingExecutableConfiguration {

  /**
   * @return the string representation of the platform that we're currently targeting. Implementors
   *     should supply one for at least each of (MacOSX, Windows X64, Linux)
   */
  String getPlatform();

  /** @return the directory that we're placing the generated executable into */
  File getWorkspace();

  /**
   * @return the archive directory is the directory which we will compress into a self-extracting
   *     executable targeting {@link #getPlatform()}
   */
  File getArchiveDirectory();

  /**
   * @return the file that we will attempt to execute upon extraction of {@link
   *     #getArchiveDirectory()}
   */
  String getExecutableFile();

  File getArchiveBase();

  /**
   * @return the executable file configuration. There are some sub-configurations (such as Windows
   *     Manifests files) that are not applicable to every platform, but we try our best
   */
  ExecutableFileConfiguration getExecutableFileConfiguration();
}
