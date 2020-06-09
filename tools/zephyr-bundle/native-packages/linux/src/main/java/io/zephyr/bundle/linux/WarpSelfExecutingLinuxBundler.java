package io.zephyr.bundle.linux;

import static java.lang.String.format;

import io.zephyr.bundle.sfx.BundleOptions;
import io.zephyr.bundle.sfx.Log;
import io.zephyr.bundle.sfx.SelfExecutingBundler;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import lombok.val;

public class WarpSelfExecutingLinuxBundler implements SelfExecutingBundler {
  @Override
  public File load(File workspaceDirectory, Log log) {

    val file = new File(workspaceDirectory, "warp").getAbsoluteFile();
    if (!exists(workspaceDirectory)) {
      val currentFile = getCodeLocation();
      try (val inputStream = currentFile.openStream()) {
        log.info(format("Copying file '%s' to '%s'", "linux/exe/warp", file));
        Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        log.info(format("Successfully copied file '%s' to '%s'", "linux/exe/warp", file));
        log.info(format("Making file '%s' executable...", file));
        setExecutable(file.toPath());
        log.info(format("Successfully set '%s' to be executable", file));
        return file;
      } catch (IOException ex) {
        throw new IllegalArgumentException("Failed to extract bundler: ", ex);
      }
    } else {
      log.info(format("File '%s' already exists--not re-extracting", file.getAbsolutePath()));
      return file;
    }
  }

  private void setExecutable(Path path) throws IOException {
    val permissions = new HashSet<PosixFilePermission>();
    permissions.add(PosixFilePermission.OWNER_EXECUTE);
    permissions.add(PosixFilePermission.OWNER_WRITE);
    Files.setPosixFilePermissions(path, permissions);
  }

  @Override
  public void create(BundleOptions options, Log log) {
    logOptions(options, log);
    val executableFile = options.getExecutableFile();
    val archiveDirectory = options.getArchiveDirectory();
    val outputFile = options.getOutputFile();
    checkFile(executableFile, log);
    checkDirectory(archiveDirectory, log);
    checkDirectory(outputFile.getParent(), log);

    val actualOutputFile = getOutputFile(options.getOutputFile());

    doRun(options, log, executableFile, archiveDirectory, actualOutputFile);
  }

  private String getArchitectureString(BundleOptions options) {
    return String.format("%s-%s", options.getPlatform().toString().toLowerCase(), "x64");
  }

  private Path getOutputFile(Path outputFile) {
    val baseName = outputFile.getFileName();
    val parentDir = outputFile.getParent();
    return Paths.get(parentDir.toAbsolutePath().toString(), format("%s.exe", baseName));
  }

  @Override
  public boolean isApplicableTo(
      BundleOptions.Platform platform, BundleOptions.Architecture architecture) {
    return platform == BundleOptions.Platform.Linux
        && architecture == BundleOptions.Architecture.X64;
  }

  private URL getCodeLocation() {
    return WarpSelfExecutingLinuxBundler.class.getClassLoader().getResource("exe/linux/warp");
  }

  private boolean exists(File workspaceDirectory) {
    return Files.exists(Paths.get(workspaceDirectory.getAbsolutePath(), "warp"));
  }

  private void logOptions(BundleOptions options, Log log) {
    log.info(
        format(
            "Attempting to generate self-extracting archive with parameters: "
                + "\t archive source directory: %s\n"
                + "\t executable file: %s\n"
                + "\t archive file base (will append '.exe'): %s\n"
                + "\t target platform: %s\n"
                + "\t target architecture: %s\n ",
            options.getArchiveDirectory(),
            options.getExecutableFile(),
            options.getOutputFile(),
            options.getPlatform(),
            options.getArchitecture()));
  }

  private void checkDirectory(Path directory, Log log) {

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

  private void checkFile(Path file, Log log) {
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

  private void doRun(
      BundleOptions options,
      Log log,
      Path executableFile,
      Path archiveDirectory,
      Path actualOutputFile) {

    val args = getInputArguments(options, executableFile, archiveDirectory, actualOutputFile);

    log.info(format("Generation command: \n%s\n", String.join(" ", args)));
    val procBuilder = new ProcessBuilder(args).inheritIO();

    try {
      val process = procBuilder.start();
      val result = process.waitFor();
      if (result != 0) {
        throw new IllegalArgumentException("Error: failed to create self-extracting archive");
      }
    } catch (IOException ex) {
      log.warn(format("Failed to create self-extracting archive.  Reason: %s", ex.getMessage()));
    } catch (InterruptedException e) {
      log.warn(format("Interrupted.  Reason: %s", e.getMessage()));
    }
  }

  private String[] getInputArguments(
      BundleOptions options, Path executableFile, Path archiveDirectory, Path actualOutputFile) {
    return new String[] {
      options.getGeneratorExecutable().getAbsolutePath(),
      "-a",
      getArchitectureString(options),
      "-i",
      archiveDirectory.toString(),
      "-e",
      executableFile.toString(),
      "-o",
      actualOutputFile.toString()
    };
  }
}
