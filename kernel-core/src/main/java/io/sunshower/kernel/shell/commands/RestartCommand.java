package io.sunshower.kernel.shell.commands;

import io.sunshower.kernel.shell.Command;
import picocli.CommandLine;

@CommandLine.Command(name = "restart")
public class RestartCommand extends Command {
  @Override
  protected int execute() {
    throw new RestartException();
  }
}
