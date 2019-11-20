package io.zephyr.kernel.command.commands.kernel;

import io.zephyr.kernel.command.DefaultCommand;
import picocli.CommandLine;

@CommandLine.Command(subcommands = KernelStartCommand.class)
public class KernelCommandSet extends DefaultCommand {
  public KernelCommandSet() {
    super("kernel");
  }
}
