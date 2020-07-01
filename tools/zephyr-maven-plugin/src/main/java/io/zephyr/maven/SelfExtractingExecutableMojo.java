package io.zephyr.maven;

import io.zephyr.bundle.sfx.BundleOptions;
import io.zephyr.bundle.sfx.Log;
import io.zephyr.bundle.sfx.SelfExecutingBundler;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.apache.commons.lang3.SystemUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import static java.lang.String.format;

@Mojo(
    name = "generate-sfx",
    defaultPhase = LifecyclePhase.PROCESS_RESOURCES,
    executionStrategy = "always")
public class SelfExtractingExecutableMojo extends AbstractMojo {

  @Getter
  @Setter
  @Parameter(
      required = true,
      name = "platform",
      alias = "platform",
      property = "generate-sfx.platform")
  private String platform;

  /**
   * since the actual file-name is platform-dependent, this property constitutes the base path of
   * the archive (such as the "target/aire" component of the path "target/aire.exe")
   */
  @Getter
  @Setter
  @Parameter(name = "archive-base", alias = "archive-base", property = "generate-sfx.archive-base")
  private File archiveBase;

  /**
   * Every executable generated must perform some steps upon execution. For instance, it's typical
   * to launch an installer such as IZPack. This file should contain instructions for doing so
   */
  @Getter
  @Setter
  @Parameter(alias = "executable-file", property = "generate-sfx.executable-file")
  private String executableFile;

  /**
   * the archive-directory property specifies which directory we're archiving and making executable
   */
  @Getter
  @Setter
  @Parameter(
      name = "archive-directory",
      alias = "archive-directory",
      property = "generate-sfx.archive-directory",
      defaultValue = "${project.basedir}/archive")
  private File archiveDirectory;

  /** this property specifies which directory we're placing the resulting executable archive into */
  @Getter
  @Setter
  @Parameter(
      required = true,
      name = "workspace",
      alias = "workspace",
      property = "generate-sfx.workspace",
      defaultValue = "${project.build.directory}/sfx-workspace")
  private File workspace;

  @Getter
  @Setter
  @Parameter(
      name = "executable-configuration",
      alias = "executable-configuration",
      property = "generate-sfx.executable-configuration")
  private ExecutableConfiguration executableConfiguration;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    verifyOutputDirectory();
    val loader = ServiceLoader.load(SelfExecutingBundler.class);
    val platform = getPlatform();
    val architecture = getArchitecture();

    for (val service : loader) {
      if (service.isApplicableTo(platform, architecture)) {
        val actualPlatform = parsePlatformOption();
        getLog()
            .info(
                format(
                    "applying service '%s' to platform '%s', arch '%s'",
                    service, actualPlatform, architecture));
        handle(service, actualPlatform, architecture);
        break;
      }
    }
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

  BundleOptions.Platform getPlatform() throws MojoFailureException {
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

  Path resolveFile(File source, String name) throws MojoFailureException {

    if (source == null) {
      throw new MojoFailureException(format("Error:  file '%s' must not be null", name));
    }
    checkExists(source, name);
    checkIsFile(source, name);
    return source.getAbsoluteFile().toPath();
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

  private void checkIsFile(File source, String name) throws MojoFailureException {
    if (!source.isFile()) {
      val message = format("%s at location %s is not a file.  Can't continue", name, source);
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

  private void handle(
      SelfExecutingBundler service,
      BundleOptions.Platform platform,
      BundleOptions.Architecture architecture)
      throws MojoFailureException {
    val log = new SimpleLogger();
    val executable = service.load(workspace, log);
    service.create(getBundleOptions(platform, architecture, executable), log);
  }

  private Path createOutputFile() throws MojoFailureException {
    return archiveBase.getAbsoluteFile().toPath();
  }

  class SimpleLogger implements Log {

    @Override
    public void warn(String s) {
      getLog().warn(s);
    }

    @Override
    public void debug(String s) {
      getLog().debug(s);
    }

    @Override
    public void info(String s) {
      getLog().info(s);
    }
  }
}
