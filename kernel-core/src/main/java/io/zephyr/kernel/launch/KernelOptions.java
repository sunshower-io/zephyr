package io.zephyr.kernel.launch;

import io.zephyr.kernel.Options;
import io.zephyr.kernel.core.AbstractValidatable;
import io.zephyr.kernel.misc.SuppressFBWarnings;
import java.io.File;
import java.util.logging.Level;
import lombok.Getter;
import lombok.Setter;
import picocli.CommandLine;

@SuppressFBWarnings
public class KernelOptions extends AbstractValidatable<KernelOptions>
    implements Options<KernelOptions> {

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
  @CommandLine.Option(names = {"-h", "--home-directory"})
  private File homeDirectory;

  /** Specify the maximum number of threads the Sunshower Kernel may start for gyre */
  @Getter
  @CommandLine.Option(
    names = {"-c", "--max-concurrency"},
    defaultValue = "8",
    type = Integer.class
  )
  private Integer concurrency = 8;

  /** Specify the maximum number of threads the Sunshower Kernel may start for kernel use */
  @Getter
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
}
