package io.zephyr.kernel.launch;

import io.zephyr.common.io.FilePermissionChecker;
import io.zephyr.common.io.Files;
import io.zephyr.common.io.Strings;
import io.zephyr.kernel.Options;
import io.zephyr.kernel.core.AbstractValidatable;
import io.zephyr.kernel.core.SunshowerKernel;
import io.zephyr.kernel.log.Logging;
import io.zephyr.kernel.misc.SuppressFBWarnings;
import java.io.File;
import java.nio.file.AccessDeniedException;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import picocli.CommandLine;

@SuppressFBWarnings
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class KernelOptions extends AbstractValidatable<KernelOptions>
    implements Options<KernelOptions> {

  static final String ZEPHYR_HOME_SYSTEM_PROPERTY_KEY = "zephyr.options.home";
  static final String ZEPHYR_HOME_ENVIRONMENT_VARIABLE_KEY = "ZEPHYR_HOME";
  private static final Logger log = Logging.get(KernelOptions.class);
  private static final long serialVersionUID = -4797996962045876401L;

  public static class SystemProperties {
    public static final String SUNSHOWER_HOME = "sunshower.home";
  }

  public static class EnvironmentVariables {
    public static final String SUNSHOWER_HOME = "SUNSHOWER_HOME";
  }

  /**
   * Specify the home directory for Sunshower.io. Sunshower data is stored here. For clustered
   * Sunshower.io kernels, this should be a distributed directory unless a data-distribution module
   * is installed
   */
  @Getter
  @Setter
  @CommandLine.Option(
    names = {"-h", "--home-directory"},
    defaultValue = "zephyr"
  )
  private File homeDirectory;

  /** Specify the maximum number of threads the Sunshower Kernel may start for gyre */
  @Getter
  @Setter
  @CommandLine.Option(
    names = {"-c", "--max-concurrency"},
    defaultValue = "8",
    type = Integer.class
  )
  private Integer concurrency = 8;

  /** Specify the maximum number of threads the Sunshower Kernel may start for kernel use */
  @Getter
  @Setter
  @CommandLine.Option(
    names = {"-k", "--kernel-concurrency"},
    defaultValue = "2",
    type = Integer.class
  )
  private Integer kernelConcurrency = 2;

  /** Specify logging level. Defaults to Level.WARNING */
  @Getter
  @Setter
  @CommandLine.Option(
    names = {"-l", "--log-level"},
    converter = LogLevelConverter.class
  )
  private Level logLevel = Level.WARNING;

  public KernelOptions() {
    registerStep(KernelOptionsValidations.homeDirectory());
  }

  public static File getKernelRootDirectory() throws AccessDeniedException {
    return resolveRoot();
  }

  private static File resolveRoot() throws AccessDeniedException {
    log.log(Level.INFO, "filesystem.resolve.root.system_properties.begin");
    val systemProperty = System.getProperty(ZEPHYR_HOME_SYSTEM_PROPERTY_KEY);

    if (Strings.isNullOrEmpty(systemProperty)) {
      log.log(Level.INFO, "filesystem.resolve.root.system_properties.doesnt_exist");
    } else {
      val file = new File(systemProperty);
      if (checkPermissions(file, "system properties")) {
        log.log(
            Level.INFO,
            "filesystem.resolve.root.system_properties.success",
            file.getAbsolutePath());
        return file;
      }
    }

    val envVariable = System.getenv(ZEPHYR_HOME_ENVIRONMENT_VARIABLE_KEY);
    if (Strings.isNullOrEmpty(envVariable)) {
      log.log(Level.INFO, "filesystem.resolve.root.env_var.doesnt_exist");
    } else {
      val file = new File(envVariable);
      if (checkPermissions(file, "ENVIRONMENT")) {
        log.log(Level.INFO, "filesystem.resolve.root.env_var.success", file.getAbsolutePath());
        return file;
      }
    }

    return Files.check(
        SunshowerKernel.getKernelOptions().getHomeDirectory(),
        FilePermissionChecker.Type.READ,
        FilePermissionChecker.Type.WRITE,
        FilePermissionChecker.Type.EXECUTE);
  }

  @SuppressWarnings("PMD.UnusedPrivateMethod")
  private static boolean checkPermissions(File file, String location) {
    if (!file.exists()) {
      log.log(
          Level.WARNING, "filesystem.resolve.root.does_not_exist", new Object[] {file, location});
      return false;
    }
    if (!file.isDirectory()) {
      log.log(
          Level.WARNING,
          "filesystem.resolve.root.is_not_directory",
          new Object[] {file.getAbsolutePath(), location});
      return false;
    }

    if (!file.canRead()) {
      log.log(
          Level.WARNING,
          "filesystem.resolve.root.permissions_failed",
          new Object[] {
            file.getAbsolutePath(), "system properties", System.getProperty("user.name"), "READ"
          });
      return false;
    }

    if (!file.canWrite()) {

      log.log(
          Level.WARNING,
          "filesystem.resolve.root.permissions_failed",
          new Object[] {
            file.getAbsolutePath(), "system properties", System.getProperty("user.name"), "WRITE"
          });
      return false;
    }

    if (!file.canExecute()) {

      log.log(
          Level.WARNING,
          "filesystem.resolve.root.permissions_failed",
          new Object[] {
            file.getAbsolutePath(), "system properties", System.getProperty("user.name"), "EXECUTE"
          });
      return false;
    }

    return true;
  }
}
