package io.zephyr.kernel.modules.shell;

import io.zephyr.kernel.Options;
import io.zephyr.kernel.core.ValidationErrors;
import io.zephyr.kernel.core.ValidationException;
import io.zephyr.kernel.core.ValidationStep;
import java.io.File;
import lombok.Getter;
import lombok.Setter;
import picocli.CommandLine;

public class ShellOptions implements Options<ShellOptions> {

  @Getter
  @Setter
  @CommandLine.Option(names = {"-i", "--interactive"})
  private boolean interactive;

  /** run this in server mode? */
  @Getter
  @Setter
  @CommandLine.Option(names = {"-s", "--server"})
  private boolean server;

  /** Port to run Zephyr server on */
  @Getter
  @Setter
  @CommandLine.Option(names = {"-p", "--port"})
  private int port = 9999;

  /** the commands to execute, if any */
  @Getter
  @Setter
  @CommandLine.Parameters(paramLabel = "commands", index = "0..*")
  private String[] commands;

  @Getter
  @Setter
  @CommandLine.Option(names = {"-h", "--home-directory"})
  private File homeDirectory;

  @Override
  public ShellOptions getTarget() {
    return this;
  }

  @Override
  public void validate() throws ValidationException {}

  @Override
  public void notify(ValidationErrors error, ValidationStep<ShellOptions> sourceStep) {}
}
