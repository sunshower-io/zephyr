package io.zephyr.kernel.command.commands.plugin;

import io.zephyr.api.CommandContext;
import io.zephyr.api.Console;
import io.zephyr.api.Result;
import io.zephyr.kernel.command.AbstractCommand;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.core.KernelLifecycle;
import lombok.val;
import picocli.CommandLine;

@CommandLine.Command(name = "list")
public class ListPluginCommand extends AbstractCommand {
  public ListPluginCommand() {
    super("list");
  }

  @Override
  public Result execute(CommandContext context) {

    val console = context.getService(Console.class);
    val kernel = context.getService(Kernel.class);

    if (kernel == null || kernel.getLifecycle().getState() != KernelLifecycle.State.Running) {
      console.errorln("Kernel is not running");
      return Result.failure();
    }

    val manager = kernel.getModuleManager();

    console.successln("Modules installed:");
    for (val module : manager.getModules()) {
      console.successln(
          "\t{0} | state {1}",
          module.getCoordinate().toCanonicalForm(), module.getLifecycle().getState());
    }

    return Result.success();
  }
}
