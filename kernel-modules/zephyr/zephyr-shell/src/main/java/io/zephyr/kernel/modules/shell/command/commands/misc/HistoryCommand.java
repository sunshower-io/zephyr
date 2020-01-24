package io.zephyr.kernel.modules.shell.command.commands.misc;

import io.zephyr.kernel.modules.shell.command.AbstractCommand;
import io.zephyr.kernel.modules.shell.console.CommandContext;
import io.zephyr.kernel.modules.shell.console.Console;
import io.zephyr.kernel.modules.shell.console.Invoker;
import io.zephyr.kernel.modules.shell.console.Result;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.val;
import picocli.CommandLine;

@CommandLine.Command(name = "history")
public class HistoryCommand extends AbstractCommand {
  static final Logger log = Logger.getLogger(HistoryCommand.class.getName());
  private static final long serialVersionUID = -4210026999188041130L;

  public HistoryCommand() {
    super("history");
  }

  @Override
  public Result execute(CommandContext context) {
    try {
      val invoker = context.getService(Invoker.class);
      val console = context.getService(Console.class);
      val history = invoker.getHistory();
      console.successln("History:");
      for (val command : history.getHistory()) {
        console.successln("\t{0}", command.getName());
      }
      return Result.success();
    } catch (Exception ex) {
      log.log(Level.INFO, "Failed to retrieve history", ex);
      return Result.failure();
    }
  }
}
