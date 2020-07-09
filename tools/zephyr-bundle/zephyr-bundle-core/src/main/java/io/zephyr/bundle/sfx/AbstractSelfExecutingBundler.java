package io.zephyr.bundle.sfx;

import static io.zephyr.bundle.sfx.IOUtilities.checkDirectory;
import static io.zephyr.bundle.sfx.IOUtilities.checkFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import lombok.val;

public abstract class AbstractSelfExecutingBundler implements SelfExecutingBundler {

  final Class<? extends SelfExecutingBundler> implementation;

  /** could use getClass(), but I like how this is more explicit. */
  protected AbstractSelfExecutingBundler(Class<? extends SelfExecutingBundler> implementation) {
    this.implementation = implementation;
  }

  @Override
  public File load(File workspaceDirectory, Log log) {

    val file = new File(workspaceDirectory, "warp").getAbsoluteFile();
    if (!exists(workspaceDirectory)) {
      val path = getBundledResourcePath();
      val currentFile = getCodeLocation(path);
      try (val inputStream = currentFile.openStream()) {
        log.info("Copying file '%s' to '%s'", path, file);
        Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        log.info("Successfully copied file '%s' to '%s'", path, file);
        log.info("Making file '%s' executable...", file);
        setExecutable(file.toPath());
        log.info("Successfully set '%s' to be executable", file);
        return file;
      } catch (IOException ex) {
        throw new IllegalArgumentException("Failed to extract bundler: ", ex);
      }
    } else {
      log.info("File '%s' already exists--not re-extracting", file.getAbsolutePath());
      return file;
    }
  }

  protected abstract String getBundledResourcePath();

  @Override
  public File create(BundleOptions options, Log log) {
    logOptions(options, log);
    val archiveDirectory = options.getArchiveDirectory();
    val executableFile = archiveDirectory.resolve(options.getExecutableFile());
    val outputFile = options.getOutputFile();

    checkFile(executableFile, log);
    checkDirectory(archiveDirectory, log);
    checkDirectory(outputFile.getParent(), log);

    val actualOutputFile = getOutputFile(options.getOutputFile(), options.getPlatform());

    doRun(options, log, executableFile, archiveDirectory, actualOutputFile);
    return actualOutputFile.toFile();
  }

  protected String getArchitectureString(BundleOptions options) {
    return String.format("%s-%s", options.getPlatform().toString().toLowerCase(), "x64");
  }

  protected Path getOutputFile(Path outputFile, BundleOptions.Platform platform) {
    val baseName = outputFile.getFileName();
    val parentDir = outputFile.getParent();
    return Paths.get(
        parentDir.toAbsolutePath().toString(), platform.normalize(baseName.toString()));
  }

  protected URL getCodeLocation(String path) {
    return implementation.getClassLoader().getResource(path);
  }

  private void logOptions(BundleOptions options, Log log) {
    log.info(
        "Attempting to generate self-extracting archive with parameters: "
            + "\t archive source directory: %s\n"
            + "\t executable file: %s\n"
            + "\t archive file base: %s\n"
            + "\t target platform: %s\n"
            + "\t target architecture: %s\n ",
        options.getArchiveDirectory(),
        options.getExecutableFile(),
        options.getOutputFile(),
        options.getPlatform(),
        options.getArchitecture());
  }

  private void doRun(
      BundleOptions options,
      Log log,
      Path executableFile,
      Path archiveDirectory,
      Path actualOutputFile) {

    try {
      val args = getInputArguments(options, executableFile, archiveDirectory, actualOutputFile);
      log.info("Generation command: \n%s\n", String.join(" ", args));
      val procBuilder = new ProcessBuilder(args).inheritIO();
      val process = procBuilder.start();
      val result = process.waitFor();
      if (result != 0) {
        throw new IllegalArgumentException("Error: failed to create self-extracting archive");
      }
    } catch (IOException ex) {
      log.warn("Failed to create self-extracting archive.  Reason: %s", ex.getMessage());
    } catch (InterruptedException e) {
      log.warn("Interrupted.  Reason: %s", e.getMessage());
    }
  }

  protected String[] getInputArguments(
      BundleOptions options, Path executableFile, Path archiveDirectory, Path actualOutputFile)
      throws IOException {
    return new String[] {
      options.getGeneratorExecutable().getAbsolutePath(),
      "-a",
      getArchitectureString(options),
      "-i",
      archiveDirectory.toString(),
      "-e",
      getExecutableFile(executableFile),
      "-o",
      actualOutputFile.toString()
    };
  }

  protected void setExecutable(Path path) throws IOException {
    val permissions = new HashSet<PosixFilePermission>();
    permissions.add(PosixFilePermission.OWNER_EXECUTE);
    permissions.add(PosixFilePermission.OWNER_WRITE);
    permissions.add(PosixFilePermission.OWNER_READ);
    Files.setPosixFilePermissions(path, permissions);
  }

  protected String getExecutableFile(Path executableFile) throws IOException {
    setExecutable(executableFile);
    return Paths.get(executableFile.toString()).getFileName().toString();
  }

  protected boolean exists(File workspaceDirectory) {
    return Files.exists(Paths.get(workspaceDirectory.getAbsolutePath(), "warp"));
  }
}
