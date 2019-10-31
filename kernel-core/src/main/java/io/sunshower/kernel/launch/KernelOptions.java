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

  @Getter
  @Setter
  @CommandLine.Option(names = {"-h", "--home-directory"})
  private File homeDirectory;

  @Getter
  @CommandLine.Option(
    names = {"-c", "--max-concurrency"},
    defaultValue = "2",
    type = Integer.class
  )
  private int concurrency = 8;

  public KernelOptions() {
    registerStep(KernelOptionsValidations.homeDirectory());
  }
}
