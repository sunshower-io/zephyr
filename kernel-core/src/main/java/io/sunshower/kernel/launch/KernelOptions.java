package io.sunshower.kernel.launch;

import io.sunshower.kernel.core.AbstractValidatable;
import java.io.File;
import lombok.Getter;
import lombok.Setter;
import picocli.CommandLine;

public class KernelOptions extends AbstractValidatable<KernelOptions> {

  public interface SystemProperties {
    String SUNSHOWER_HOME = "sunshower.home";
  }

  public interface EnvironmentVariables {
    String SUNSHOWER_HOME = "SUNSHOWER_HOME";
  }

  @Getter
  @Setter
  @CommandLine.Option(names = {"-h", "--home-directory"})
  private File homeDirectory;

  {
    registerStep(KernelOptionsValidations.homeDirectory());
  }
}
