package io.zephyr.kernel.command.commands.server;

import io.zephyr.api.CommandContext;
import io.zephyr.api.Result;
import io.zephyr.kernel.command.DefaultCommand;
import io.zephyr.kernel.server.Server;
import picocli.CommandLine;

@CommandLine.Command(name = "stop")
public class StopServerCommand extends DefaultCommand {

  public StopServerCommand() {
    super("stop");
  }

  @Override
  public Result execute(CommandContext context) {
    context.getService(Server.class).stop();
    return Result.success();
  }
}
