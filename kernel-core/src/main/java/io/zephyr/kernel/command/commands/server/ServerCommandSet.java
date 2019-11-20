package io.zephyr.kernel.command.commands.server;

import io.zephyr.kernel.command.DefaultCommand;
import picocli.CommandLine;

@CommandLine.Command(subcommands = {StopServerCommand.class})
public class ServerCommandSet extends DefaultCommand {
  private static final long serialVersionUID = -5044291743098126733L;

  public ServerCommandSet() {
    super("server");
  }
}
