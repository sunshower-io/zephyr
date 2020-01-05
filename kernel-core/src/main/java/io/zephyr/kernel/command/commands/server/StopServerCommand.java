package io.zephyr.kernel.command.commands.server;

import io.zephyr.cli.CommandContext;
import io.zephyr.cli.Result;
import io.zephyr.kernel.command.DefaultCommand;
import io.zephyr.kernel.server.Server;
import picocli.CommandLine;

@CommandLine.Command(name = "stop")
public class StopServerCommand extends DefaultCommand {

  private static final long serialVersionUID = -7901772560457432938L;

  public StopServerCommand() {
    super("stop");
  }

  @Override
  public Result execute(CommandContext context) {
    context.getService(Server.class).stop();
    return Result.success();
  }
}
