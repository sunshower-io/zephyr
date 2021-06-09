package io.zephyr.kernel.modules.shell.command.commands.server;

import io.zephyr.kernel.modules.shell.command.DefaultCommand;
import picocli.CommandLine;

@CommandLine.Command(
    name = "server",
    subcommands = {StopServerCommand.class})
public class ServerCommandSet extends DefaultCommand {
  private static final long serialVersionUID = -5044291743098126733L;

  public ServerCommandSet() {
    super("server");
  }
}
