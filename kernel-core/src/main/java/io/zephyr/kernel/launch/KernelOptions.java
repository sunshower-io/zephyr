package io.zephyr.kernel.launch;

import io.zephyr.kernel.core.AbstractValidatable;
import io.zephyr.kernel.misc.SuppressFBWarnings;
import java.io.File;
import lombok.Getter;
import lombok.Setter;
import picocli.CommandLine;

@SuppressFBWarnings
public class KernelOptions extends AbstractValidatable<KernelOptions> {

  private static final long serialVersionUID = -4797996962045876401L;

  public static class SystemProperties {
    public static final String SUNSHOWER_HOME = "sunshower.home";
  }

  public static class EnvironmentVariables {
    public static final String SUNSHOWER_HOME = "SUNSHOWER_HOME";
  }

  @Getter
  @Setter
  @CommandLine.Option(names = {"-p", "--port"})
  private int port = 9999;

  /** Start the kernel server */
  @Getter
  @Setter
  @CommandLine.Option(names = {"-s", "--server"})
  private boolean server;

  /**
   * Specify the home directory for Sunshower.io. Sunshower data is stored here. For clustered
   * Sunshower.io kernels, this should be a distributed directory unless a data-distribution module
   * is installed
   */
  @Getter
  @Setter
  @CommandLine.Option(names = {"-h", "--home-directory"})
  private File homeDirectory;

  /** Specify the maximum number of threads the Sunshower Kernel may start */
  @Getter
  @CommandLine.Option(
    names = {"-c", "--max-concurrency"},
    defaultValue = "2",
    type = Integer.class
  )
  private Integer concurrency = 8;

  /** if true, the Sunshower Kernel Launcher will start a shell */
  @Getter
  @Setter
  @CommandLine.Option(names = {"-i", "--interactive"})
  private boolean interactive;

  /** If we're not interactive, just pass 'em through and let the kernel execute them */
  @Getter
  @Setter
  @CommandLine.Parameters(paramLabel = "arguments", index = "0..*")
  private String[] parameters;

  public KernelOptions() {
    registerStep(KernelOptionsValidations.homeDirectory());
  }
}
