package io.zephyr.kernel.modules.shell.command.commands.server;

import io.zephyr.kernel.modules.shell.command.DefaultCommand;
import io.zephyr.kernel.modules.shell.console.CommandContext;
import io.zephyr.kernel.modules.shell.console.Result;
import io.zephyr.kernel.modules.shell.server.Server;
import picocli.CommandLine;

@CommandLine.Command(name = "stop")
public class StopServerCommand extends DefaultCommand {

  private static final long serialVersionUID = -7901772560457432938L;

  public StopServerCommand() {
    super("stop");
  }

  @Override
  public Result execute(CommandContext context) {
    try {
      context.getService(Server.class).stop();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return Result.success();
  }
}
