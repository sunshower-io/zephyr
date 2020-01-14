package io.zephyr.kernel.modules.shell.command.commands.plugin;

import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.core.KernelLifecycle;
import io.zephyr.kernel.modules.shell.command.AbstractCommand;
import io.zephyr.kernel.modules.shell.console.CommandContext;
import io.zephyr.kernel.modules.shell.console.Console;
import io.zephyr.kernel.modules.shell.console.Result;
import lombok.val;
import picocli.CommandLine;

@CommandLine.Command(name = "list")
public class ListPluginCommand extends AbstractCommand {
  private static final long serialVersionUID = -7604400606672779606L;

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
          "\t%s | state %s",
          module.getCoordinate().toCanonicalForm(), module.getLifecycle().getState());
    }

    return Result.success();
  }
}
