package io.zephyr.bundle.linux;

import static io.zephyr.bundle.sfx.IOUtilities.unzipDirectory;
import static java.lang.String.format;

import io.zephyr.bundle.sfx.*;
import io.zephyr.bundle.sfx.icons.ImageBundler;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ServiceLoader;
import lombok.val;

public class LinuxWindowsExecutableFileIconService implements ExecutableFileIconService {

  @Override
  public boolean isApplicableTo(
      BundleOptions.Platform platform, BundleOptions.Architecture architecture) {
    return platform == BundleOptions.Platform.Linux;
  }

  @Override
  public void setIcons(SelfExtractingExecutableConfiguration configuration, Log log) {
    log.info("Configuring executable metadata for platform '%s'", configuration.getPlatform());
    try {
      unpackIfNecessary(configuration, log);
      val executableFile =
          new File(configuration.getArchiveBase().getAbsoluteFile() + ".exe")
              .toPath()
              .toAbsolutePath()
              .toString();
      val workspace = configuration.getWorkspace().toPath();
      setIconIfApplicable(
          executableFile,
          configuration,
          workspace,
          workspace.resolve(Paths.get("rcedit.exe")),
          log);
    } catch (IOException e) {
      log.warn("Failed to execute file icon service.  Reason: %s", e.getMessage());
    }
  }

  private void setIconIfApplicable(
      String executableFile,
      SelfExtractingExecutableConfiguration configuration,
      Path workspace,
      Path emulatedExecutable,
      Log log) {

    val emulator = workspace.resolve(Paths.get("wine", "wine", "wine"));

    if (!(Files.exists(emulator))) {
      log.warn(
          "Error: misconfigured or corrupt installation.  Could not find emulator at '%s'",
          emulator);
      return;
    }

    if (!Files.isExecutable(emulator)) {
      log.warn("Error: misconfigured or corrupt installation.  Emulator at '%s' is not executable");
    }
    val executableConfiguration = configuration.getExecutableFileConfiguration();

    if (executableConfiguration == null) {
      log.info("No executable configuration found...not doing anything");
      return;
    }

    val iconFile = generateIcon(configuration, executableConfiguration, workspace, log);
    if (iconFile == null) {
      return;
    }
    val processBuilder =
        new ProcessBuilder()
            .inheritIO()
            .command(
                emulator.toAbsolutePath().toString(),
                emulatedExecutable.toAbsolutePath().toString(),
                executableFile,
                "--set-icon",
                iconFile.getAbsolutePath());

    try {
      val process = processBuilder.start();
      val code = process.waitFor();

      if (code != 0) {
        log.warn(
            "Something may have gone wrong--please check the executable to verify that the icon has been set");
      } else {
        log.info("Successfully set icon");
      }
    } catch (Exception ex) {
      log.warn("Encountered exception: %s", ex.getMessage());
    }
  }

  private File generateIcon(
      SelfExtractingExecutableConfiguration selfExtractingExecutableConfiguration,
      ExecutableFileConfiguration executableConfiguration,
      Path workspace,
      Log log) {
    val iconDefinition = executableConfiguration.getIconDefinition();

    if (iconDefinition == null) {
      log.info("No icon definition found.  Not doing anything");
      return null;
    }
    val format = iconDefinition.getFormat();
    log.info("Attempting to resolve an icon generator for format: %s", format);

    val serviceLoader = ServiceLoader.load(ImageBundler.class);

    for (val bundler : serviceLoader) {
      if (bundler.supports(iconDefinition.getFormat())) {
        log.info("Applying bundler '%s' to format '%s'", bundler, format);
        return bundler.bundle(selfExtractingExecutableConfiguration, workspace, log);
      }
    }
    log.warn("No suitable icon generators found for format '%s'", format);
    return null;
  }

  private void unpackIfNecessary(SelfExtractingExecutableConfiguration configuration, Log log)
      throws IOException {
    val workspaceDirectory = configuration.getWorkspace();
    val workspacePath = workspaceDirectory.toPath();
    IOUtilities.checkDirectory(workspacePath, log);

    val zipFile = workspacePath.resolve("wine.zip");
    val extracted = workspacePath.resolve("wine");

    val executableFile = workspacePath.resolve("rcedit.exe");
    boolean unarchived = false, unpacked = false, rcexecutable = false;
    if (Files.exists(zipFile) && Files.isRegularFile(zipFile)) {
      log.info("Platform archive exists...continuing");
      unarchived = true;
    }

    if (Files.exists(extracted) && Files.isDirectory(extracted)) {
      log.info("Platform archive is unpacked...continuing");
      unpacked = true;
    }

    if (!Files.exists(executableFile)) {
      rcexecutable = false;
    }

    if (unarchived && unpacked) {
      return;
    }

    if (!unarchived) {
      unarchive(workspacePath, log);
    }

    if (!unpacked) {
      unpack(workspacePath, log);
    }

    if (!rcexecutable) {
      extractResourceEditor(workspacePath, log);
    }
  }

  private void extractResourceEditor(Path workspacePath, Log log) throws IOException {
    log.info("Extracting resource editor...");
    val internalUrl =
        LinuxWindowsExecutableFileIconService.class
            .getClassLoader()
            .getResource("exe/linux/rcedit.exe");
    if (internalUrl == null) {
      throw new IllegalStateException(
          "Error: this package is malformed.  Please report to https://github.com/zephyr");
    }

    val outputFile = workspacePath.resolve("rcedit.exe");
    if (!Files.exists(outputFile)) {
      Files.createFile(outputFile);
    }
    try (val inputStream = internalUrl.openStream()) {
      Files.copy(inputStream, outputFile, StandardCopyOption.REPLACE_EXISTING);
    }
    log.info("Successfully extracted resource editor");
  }

  private void unpack(Path workspacePath, Log log) throws IOException {
    val zipfile = workspacePath.resolve("wine.zip");
    if (!(Files.exists(zipfile) || Files.isRegularFile(zipfile))) {
      log.info("archive '%s' doesn't exist for some reason", zipfile);
      throw new IllegalArgumentException(
          "Error: native handler doesn't exist.  This is almost certainly a bug, but you may try re-building after cleaning");
    }

    var destination = workspacePath.resolve("wine");
    if (!Files.exists(destination)) {
      destination = Files.createDirectory(destination);
    }

    if (!Files.isDirectory(destination)) {
      throw new IllegalArgumentException(
          format("Error:  Expected '%s' to be a directory, it wasn't", destination));
    }
    unzipDirectory(zipfile, destination);
  }

  private void unarchive(Path workspacePath, Log log) throws IOException {
    log.info("Preparing native handler...");
    val internalUrl =
        LinuxWindowsExecutableFileIconService.class
            .getClassLoader()
            .getResource("exe/linux/wine.zip");
    if (internalUrl == null) {
      throw new IllegalStateException(
          "Error: this package is malformed.  Please report to https://github.com/zephyr");
    }

    val outputFile = workspacePath.resolve("wine.zip");
    if (!Files.exists(outputFile)) {
      Files.createFile(outputFile);
    }

    try (val inputStream = internalUrl.openStream()) {
      Files.copy(inputStream, outputFile, StandardCopyOption.REPLACE_EXISTING);
    }
    log.info("Successfully extracted native handler");
  }
}
