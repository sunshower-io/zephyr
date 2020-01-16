package io.zephyr.kernel.modules.shell.command.commands.plugin;

import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.core.KernelLifecycle;
import io.zephyr.kernel.core.ModuleCoordinate;
import io.zephyr.kernel.module.ModuleLifecycle;
import io.zephyr.kernel.module.ModuleLifecycleChangeGroup;
import io.zephyr.kernel.module.ModuleLifecycleChangeRequest;
import io.zephyr.kernel.modules.shell.command.AbstractCommand;
import io.zephyr.kernel.modules.shell.console.CommandContext;
import io.zephyr.kernel.modules.shell.console.Console;
import io.zephyr.kernel.modules.shell.console.Result;
import lombok.val;

@SuppressWarnings({"PMD.AvoidInstantiatingObjectsInLoops", "PMD.DataflowAnomalyAnalysis"})
public abstract class PluginLifecycleCommand extends AbstractCommand {

  private static final long serialVersionUID = -6463939543675292479L;

  protected PluginLifecycleCommand(String name) {
    super(name);
  }

  protected Result execute(
      CommandContext context, ModuleLifecycle.Actions action, String... plugins) {
    val console = context.getService(Console.class);
    val kernel = context.getService(Kernel.class);

    if (kernel == null || kernel.getLifecycle().getState() != KernelLifecycle.State.Running) {
      console.errorln("Error: kernel is not running");
      return Result.failure();
    }

    if (plugins == null || plugins.length == 0) {
      console.errorln("No plugins requested for stopping");
      return Result.failure();
    }

    val request = new ModuleLifecycleChangeGroup();
    for (val plugin : plugins) {
      try {
        val pluginRequest =
            new ModuleLifecycleChangeRequest(ModuleCoordinate.parse(plugin), action);
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
