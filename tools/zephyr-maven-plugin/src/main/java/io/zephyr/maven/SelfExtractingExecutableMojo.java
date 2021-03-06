package io.zephyr.maven;

import io.zephyr.bundle.sfx.*;
import lombok.val;
import org.apache.commons.lang3.SystemUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import javax.inject.Named;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Level;

import static java.lang.String.format;

@Named
@Mojo(
    name = "generate-sfx",
    defaultPhase = LifecyclePhase.PROCESS_RESOURCES,
    executionStrategy = "always")
public class SelfExtractingExecutableMojo extends AbstractZephyrMojo
    implements SelfExtractingExecutableConfiguration {

  @Override
  public ExecutableFileConfiguration getExecutableFileConfiguration() {
    return executableConfiguration;
  }

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    verifyOutputDirectory();
    val loader = ServiceLoader.load(SelfExecutingBundler.class);
    val platform = resolvePlatform();
    val architecture = getArchitecture();

    val executableFile = generateExecutable(loader, platform, architecture);

    editExecutable(executableFile, platform, architecture);
  }

  private void editExecutable(
      File executableFile,
      BundleOptions.Platform platform,
      BundleOptions.Architecture architecture) {
    val log = getLog();
    if (executableConfiguration == null) {
      log.info("No additional executable configuration specified--not modifying");
    }

    for (val service : ServiceLoader.load(ExecutableFileIconService.class)) {
      log.info(
          format(
              "Checking service '%s' against platform/architecture combination %s/%s",
              service, platform, architecture));

      if (service.isApplicableTo(platform, architecture)) {
        log.info(format("Service is applicable to %s/%s", platform, architecture));
        service.setIcons(this, new SimpleLogger());
      }
    }
  }

  private File generateExecutable(
      ServiceLoader<SelfExecutingBundler> loader,
      BundleOptions.Platform platform,
      BundleOptions.Architecture architecture)
      throws MojoFailureException {
    for (val service : loader) {
      if (service.isApplicableTo(platform, architecture)) {
        val actualPlatform = parsePlatformOption();
        getLog()
            .info(
                format(
                    "applying service '%s' to platform '%s', arch '%s'",
                    service, actualPlatform, architecture));
        return handle(service, actualPlatform, architecture);
      }
    }
    throw new MojoFailureException(
        format(
            "Failed to resolve any applicable service loaders for this platform (%s)", platform));
  }

  BundleOptions getBundleOptions(
      BundleOptions.Platform platform, BundleOptions.Architecture architecture, File executable)
      throws MojoFailureException {
    val archiveDirectory = resolveDirectory(this.archiveDirectory, "archive directory");

    return new BundleOptions(
        platform, architecture, executableFile, archiveDirectory, createOutputFile(), executable);
  }

  BundleOptions.Architecture getArchitecture() {
    // really our only choice RN
    return BundleOptions.Architecture.X64;
  }

  BundleOptions.Platform resolvePlatform() throws MojoFailureException {
    getLog().info("Looking up current platform architecture...");
    BundleOptions.Platform platform = null;
    if (SystemUtils.IS_OS_MAC_OSX) {
      platform = BundleOptions.Platform.MacOS;
    } else if (SystemUtils.IS_OS_WINDOWS) {
      platform = BundleOptions.Platform.Windows;
    } else if (SystemUtils.IS_OS_LINUX) {
      platform = BundleOptions.Platform.Linux;
    }

    if (platform == null) {
      throw new MojoFailureException(
          "Cannot determine current platform architecture.  Please submit a bug report");
    }

    getLog().info(format("Current platform: %s", platform));
    return platform;
  }

  BundleOptions.Platform parsePlatformOption() throws MojoFailureException {
    if (platform == null) {
      throw new MojoFailureException("Error:  platform must not be null");
    }

    val opt = platform.toLowerCase().trim();

    switch (opt) {
      case "windows":
        return BundleOptions.Platform.Windows;
      case "linux":
        return BundleOptions.Platform.Linux;
      case "macos":
        return BundleOptions.Platform.MacOS;
    }
    throw new MojoFailureException(
        format(
            "Error: platform '%s' is not supported.  Must be one of [%s]",
            platform, bundlePlatformNames()));
  }

  void verifyOutputDirectory() throws MojoFailureException {
    getLog().info(format("verifying workspace directory %s:", workspace));

    if (workspace == null) {
      throw new MojoFailureException(
          "Error: Workspace directory <null> does not exist.  Should not be here");
    }

    if (!workspace.exists()) {
      getLog()
          .info(
              format(
                  "Directory '%s' does not exist.  Attempting to create '%s' and its parents...",
                  workspace, workspace));
      if (!workspace.mkdirs()) {
        getLog().warn(format("Failed to create directory '%s' or one of its parents", workspace));
      } else {
        getLog().info(format("Successfully created directory '%s'", workspace));
      }
    }

    if (!workspace.isDirectory()) {
      getLog()
          .warn(
              format(
                  "Error: directory '%s' exists but is not a directory.  Not overwriting",
                  workspace));
      throw new MojoFailureException(
          format("Error.  File '%s' exists but is not a directory", workspace));
    }

    if (!workspace.canWrite()) {
      getLog()
          .warn(
              format(
                  "Error:  directory '%s' exists and is a directory, yet I cannot write to it",
                  workspace));
      throw new MojoFailureException(
          format(
              "Error: cannot write to directory '%s'.  Check its permissions and try again (or delete it and let me create it)",
              workspace));
    }
  }

  private Path resolveDirectory(File archiveDirectory, String name) throws MojoFailureException {
    checkExists(archiveDirectory, name);
    checkIsDirectory(archiveDirectory, name);
    return archiveDirectory.getAbsoluteFile().toPath();
  }

  private void checkIsDirectory(File archiveDirectory, String name) throws MojoFailureException {
    if (!archiveDirectory.isDirectory()) {
      val message =
          format("%s at location %s is not a directory.  Can't continue", name, archiveDirectory);
      getLog().warn(message);
      throw new MojoFailureException(message);
    }
  }

  private void checkExists(File source, String name) throws MojoFailureException {
    getLog().info(format("attempting to resolve %s at %s", name, source));
    if (!source.exists()) {

      getLog().warn(format("file '%s' does not exist", source));
      throw new MojoFailureException(
          format("Error:  file '%s' at path '%s' does not exist", name, source));
    }
  }

  private List<String> bundlePlatformNames() {

    val result = new ArrayList<String>(BundleOptions.Platform.values().length);
    for (val item : Platform.values()) {
      result.add(item.name().toLowerCase());
    }
    return result;
  }

  private File handle(
      SelfExecutingBundler service,
      BundleOptions.Platform platform,
      BundleOptions.Architecture architecture)
      throws MojoFailureException {
    val log = new SimpleLogger();
    val executable = service.load(workspace, log);
    return service.create(getBundleOptions(platform, architecture, executable), log);
  }

  private Path createOutputFile() throws MojoFailureException {
    return archiveBase.getAbsoluteFile().toPath();
  }

  class SimpleLogger implements Log {

    @Override
    public Level getLevel() {
      val log = getLog();
      if (log.isErrorEnabled()) {
        return Level.SEVERE;
      }
      if (log.isWarnEnabled()) {
        return Level.WARNING;
      }
      if (log.isInfoEnabled()) {
        return Level.INFO;
      }
      if (log.isDebugEnabled()) {
        return Level.FINEST;
      }
      return Level.INFO;
    }

    @Override
    public void warn(String s, Object... params) {
      getLog().warn(format(s, params));
    }

    @Override
    public void debug(String s, Object... params) {
      getLog().debug(format(s, params));
    }

    @Override
    public void info(String s, Object... params) {
      getLog().info(format(s, params));
    }
  }
}
