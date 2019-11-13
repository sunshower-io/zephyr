package io.sunshower.kernel.launch;

import io.sunshower.kernel.core.AbstractValidatable;
import java.io.File;
import lombok.Getter;
import lombok.Setter;
import picocli.CommandLine;

public class KernelOptions extends AbstractValidatable<KernelOptions> {

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

  /** Specify the maximum number of threads the Sunshower Kernel may start */
  @Getter
  @CommandLine.Option(
      names = {"-c", "--max-concurrency"},
      defaultValue = "2",
      type = Integer.class)
  private Integer concurrency = 8;

  /** if true, the Sunshower Kernel Launcher will start a shell */
  @Getter
  @Setter
  @CommandLine.Option(names = {"-i", "--interactive"})
  private boolean interactive;

  /**
   * If we're not interactive, just pass 'em through and let the kernel execute them
   */
  @Getter
  @Setter
  @CommandLine.Parameters(paramLabel = "arguments", index = "0..*")
  private String[] parameters;

  public KernelOptions() {
    registerStep(KernelOptionsValidations.homeDirectory());
  }
}
