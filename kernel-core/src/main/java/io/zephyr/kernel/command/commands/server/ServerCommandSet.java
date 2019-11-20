package io.zephyr.kernel.command.commands.server;

import io.zephyr.kernel.command.DefaultCommand;
import picocli.CommandLine;

@CommandLine.Command(subcommands = {StopServerCommand.class})
public class ServerCommandSet extends DefaultCommand {
  public ServerCommandSet() {
    super("server");
  }
}
