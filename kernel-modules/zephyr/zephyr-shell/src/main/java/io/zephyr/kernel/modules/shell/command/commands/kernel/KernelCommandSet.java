package io.zephyr.kernel.modules.shell.command.commands.kernel;

import io.zephyr.kernel.modules.shell.command.DefaultCommand;
import picocli.CommandLine;

@CommandLine.Command(
    name = "kernel",
    subcommands = {KernelStartCommand.class, KernelStopCommand.class, KernelRestartCommand.class})
public class KernelCommandSet extends DefaultCommand {
  private static final long serialVersionUID = 8596255314192173338L;

  public KernelCommandSet() {
    super("kernel");
  }
}
