package io.zephyr.scan;

import io.zephyr.kernel.Options;
import io.zephyr.kernel.core.ValidationErrors;
import io.zephyr.kernel.core.ValidationException;
import io.zephyr.kernel.core.ValidationStep;
import lombok.Getter;
import picocli.CommandLine;

public class DirectoryScannerOptions implements Options<DirectoryScannerOptions> {

  @Getter
  @CommandLine.Option(
    split = ",",
    names = {"-w", "--watch"}
  )
  private String[] directories;

  @Getter
  @CommandLine.Option(names = {"--scan"})
  private boolean scan;

  @Getter
  @CommandLine.Option(names = {"-install-on-start"})
  private boolean installOnStart = true;

  @Override
  public DirectoryScannerOptions getTarget() {
    return this;
  }

  @Override
  public void validate() throws ValidationException {}

  @Override
  public void notify(ValidationErrors error, ValidationStep<DirectoryScannerOptions> sourceStep) {}
}
