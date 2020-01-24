package io.zephyr.kernel.modules.shell.command.commands.kernel;

import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.core.KernelLifecycle;
import io.zephyr.kernel.modules.shell.command.AbstractCommand;
import io.zephyr.kernel.modules.shell.console.CommandContext;
import io.zephyr.kernel.modules.shell.console.Console;
import io.zephyr.kernel.modules.shell.console.Result;
import lombok.val;
import picocli.CommandLine;

@CommandLine.Command(name = KernelRestartCommand.name)
public class KernelRestartCommand extends AbstractCommand {
  static final String name = "restart";
  private static final long serialVersionUID = -5861453408138881688L;

  public KernelRestartCommand() {
    super(name);
  }

  @Override
  public Result execute(CommandContext context) {

    val console = context.getService(Console.class);
    val kernel = context.getService(Kernel.class);
    if (kernel == null || kernel.getLifecycle().getState() != KernelLifecycle.State.Running) {
      console.errorln("Error: kernel is not running");
      return Result.failure();
    }
    console.successln("Restarting kernel...");
    console.successln("Stopping kernel...");
    kernel.stop();
    console.successln("Kernel stopped");
    console.successln("Starting kernel...");
    kernel.start();
    console.successln("Kernel started");
    return Result.success();
  }
}
