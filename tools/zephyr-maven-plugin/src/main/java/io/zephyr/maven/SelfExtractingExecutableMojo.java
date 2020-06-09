package io.zephyr.maven;

import io.zephyr.bundle.sfx.BundleOptions;
import io.zephyr.bundle.sfx.Log;
import io.zephyr.bundle.sfx.SelfExecutingBundler;
import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.SystemUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.ServiceLoader;

import static java.lang.String.format;

@Mojo(name = "generate-sfx", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class SelfExtractingExecutableMojo extends AbstractMojo {

  @Getter
  @Parameter(
      required = true,
      property = "workspace",
      defaultValue = "${project.build.directory}/sfx-workspace")
  private File outputDirectory;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    verifyOutputDirectory();

    val loader = ServiceLoader.load(SelfExecutingBundler.class, ClassLoader.getSystemClassLoader());
    val platform = getPlatform();
    val architecture = getArchitecture();

    for (val service : loader) {
      if (service.isApplicableTo(platform, architecture)) {
        getLog()
            .info(
                format(
                    "applying service '%s' to platform '%s', arch '%s'",
                    service, platform, architecture));
        handle(service);
        break;
      }
    }
  }

  private void handle(SelfExecutingBundler service) {
    val log = new SimpleLogger();
    service.load(outputDirectory, log);
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

    getLog().info(format("Current platform: ", platform));
    return platform;
  }

  void verifyOutputDirectory() throws MojoFailureException {
    getLog().info("verifying workspace directory {}:");

    if (outputDirectory == null) {
      throw new MojoFailureException(
          format("Error: Workspace directory <null> does not exist.  Should not be here"));
    }

    if (!outputDirectory.exists()) {
      getLog()
          .info(
              format(
                  "Directory '%s' does not exist.  Attempting to create '%s' and its parents...",
                  outputDirectory, outputDirectory));
      if (!outputDirectory.mkdirs()) {
        getLog()
            .warn(format("Failed to create directory '%s' or one of its parents", outputDirectory));
      } else {
        getLog().info(format("Successfull created directory '%s'", outputDirectory));
      }
    }

    if (!outputDirectory.isDirectory()) {
      getLog()
          .warn(
              format(
                  "Error: directory '%s' exists but is not a directory.  Not overwriting",
                  outputDirectory));
      throw new MojoFailureException(
          format("Error.  File '%s' exists but is not a directory", outputDirectory));
    }

    if (!outputDirectory.canWrite()) {
      getLog()
          .warn(
              format(
                  "Error:  directory '%s' exists and is a directory, yet I cannot write to it",
                  outputDirectory));
      throw new MojoFailureException(
          format(
              "Error: cannot write to directory '%s'.  Check its permissions and try again (or delete it and let me create it)"));
    }
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
