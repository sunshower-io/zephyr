package io.zephyr.bundle.sfx;

import java.nio.file.Path;
import lombok.Getter;

public class BundleOptions {
  public enum Platform {
    Windows("windows"),
    MacOS("macos"),
    Linux("linux");

    final String name;

    Platform(String name) {
      this.name = name;
    }
  }

  public enum Architecture {
    X64("x64");

    final String name;

    Architecture(String name) {
      this.name = name;
    }
  }

  /** the platform we're currently targeting */
  @Getter final Platform platform;

  /** the architecture we're currently targeting */
  @Getter final Architecture architecture;

  /** the output to generate */
  @Getter final Path outputFile;
  /** the platform-specific executable file to include */
  @Getter final Path executableFile;

  /** the directory to archive */
  @Getter final Path archiveDirectory;

  public BundleOptions(
      Platform platform,
      Architecture architecture,
      Path executableFile,
      Path archiveDirectory,
      Path outputFile) {
    this.platform = platform;
    this.architecture = architecture;
    this.executableFile = executableFile;
    this.archiveDirectory = archiveDirectory;
    this.outputFile = outputFile;
  }
}
