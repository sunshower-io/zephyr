package io.zephyr.kernel.command.commands.misc;

import io.zephyr.api.CommandContext;
import io.zephyr.api.Console;
import io.zephyr.api.Invoker;
import io.zephyr.api.Result;
import io.zephyr.kernel.command.AbstractCommand;
import lombok.val;
import picocli.CommandLine;

import java.util.logging.Level;
import java.util.logging.Logger;

@CommandLine.Command(name = "history")
public class HistoryCommand extends AbstractCommand {
  static final Logger log = Logger.getLogger(HistoryCommand.class.getName());

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
