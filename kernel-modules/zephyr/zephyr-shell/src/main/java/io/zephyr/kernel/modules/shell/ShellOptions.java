package io.zephyr.kernel.modules.shell;

import io.zephyr.kernel.Options;
import io.zephyr.kernel.core.ValidationErrors;
import io.zephyr.kernel.core.ValidationException;
import io.zephyr.kernel.core.ValidationStep;
import lombok.Getter;
import picocli.CommandLine;

public class ShellOptions implements Options<ShellOptions> {

  @Getter
  @CommandLine.Option(names = {"-i", "--interactive"})
  private boolean interactive;

  /** run this in server mode? */
  @Getter
  @CommandLine.Option(names = {"-s", "--server"})
  private boolean server;

  /** Port to run Zephyr server on */
  @Getter
  @CommandLine.Option(names = {"-p", "--port"})
  private int port = 9999;

  /** the commands to execute, if any */
  @Getter
  @CommandLine.Parameters(paramLabel = "commands", index = "0..*")
  private String[] commands;

  @Override
  public ShellOptions getTarget() {
    return this;
  }

  @Override
  public void validate() throws ValidationException {}

  @Override
  public void notify(ValidationErrors error, ValidationStep<ShellOptions> sourceStep) {}
}
