package io.zephyr.kernel.command.commands.plugin;

import io.zephyr.api.CommandContext;
import io.zephyr.api.Console;
import io.zephyr.api.Result;
import io.zephyr.kernel.command.AbstractCommand;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.core.KernelLifecycle;
import io.zephyr.kernel.core.ModuleCoordinate;
import io.zephyr.kernel.module.ModuleLifecycle;
import io.zephyr.kernel.module.ModuleLifecycleChangeGroup;
import io.zephyr.kernel.module.ModuleLifecycleChangeRequest;
import lombok.val;
import picocli.CommandLine;

@CommandLine.Command(name = StartPluginCommand.name)
public class StartPluginCommand extends AbstractCommand {
  static final String name = "start";

  @CommandLine.Parameters String[] plugins;

  public StartPluginCommand() {
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

    if (plugins == null || plugins.length == 0) {
      console.errorln("No plugins requested for stopping");
    }

    val request = new ModuleLifecycleChangeGroup();
    for (val plugin : plugins) {
      try {
        val pluginRequest =
            new ModuleLifecycleChangeRequest(
                ModuleCoordinate.parse(plugin), ModuleLifecycle.Actions.Activate);
        request.addRequest(pluginRequest);
      } catch (Exception ex) {
        console.errorln("Plugin coordinate ''{0}'' is not valid", plugin);
      }
    }
    try {
      kernel.getModuleManager().prepare(request).commit().toCompletableFuture().get();
      return Result.success();
    } catch (Exception ex) {
      console.errorln("Failed to modify plugin lifecycle.  Reason: {0}", ex.getMessage());
      return Result.failure();
    }
  }
}
