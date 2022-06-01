package io.zephyr.kernel.modules.shell.command.commands.plugin;

import static io.zephyr.api.ModuleEvents.INSTALLED;
import static io.zephyr.api.ModuleEvents.INSTALLING;
import static io.zephyr.api.ModuleEvents.INSTALL_FAILED;
import static io.zephyr.kernel.core.actions.ModulePhaseEvents.MODULE_SET_INSTALLATION_COMPLETED;
import static io.zephyr.kernel.core.actions.ModulePhaseEvents.MODULE_SET_INSTALLATION_INITIATED;

import io.sunshower.lang.events.Event;
import io.sunshower.lang.events.EventListener;
import io.sunshower.lang.events.EventType;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.core.ModuleCoordinate;
import io.zephyr.kernel.module.ModuleLifecycle.Actions;
import io.zephyr.kernel.module.ModuleLifecycleChangeGroup;
import io.zephyr.kernel.module.ModuleLifecycleChangeRequest;
import io.zephyr.kernel.modules.shell.command.AbstractCommand;
import io.zephyr.kernel.modules.shell.console.Color;
import io.zephyr.kernel.modules.shell.console.CommandContext;
import io.zephyr.kernel.modules.shell.console.Console;
import io.zephyr.kernel.modules.shell.console.Result;
import lombok.val;
import picocli.CommandLine;

@CommandLine.Command(name = "remove")
public class RemovePluginCommand extends AbstractCommand {

  @CommandLine.Parameters private String[] coordinates;

  public RemovePluginCommand() {
    super("remove");
  }

  @Override
  public Result execute(CommandContext context) {
    val console = context.getService(Console.class);
    if (coordinates == null || coordinates.length == 0) {
      console.errorln("No plugin coordinates provided. Not doing anything");
      return Result.failure();
    }

    val kernel = context.getService(Kernel.class);
    if (kernel == null) {
      console.errorln("Kernel is not running (have you run <kernel start>?");
      return Result.failure();
    }

    val listener = new PluginRemovalListener(console);

    kernel.addEventListener(
        listener,
        INSTALLING,
        INSTALL_FAILED,
        INSTALLED,
        MODULE_SET_INSTALLATION_COMPLETED,
        MODULE_SET_INSTALLATION_INITIATED);

    val moduleManager = kernel.getModuleManager();

    val group = new ModuleLifecycleChangeGroup();
    for (val coordinate : coordinates) {
      console.writeln("Preparing to remove '%s'", new Color[] {Color.Blue}, coordinate);
      val coord = ModuleCoordinate.parse(coordinate);
      group.addRequest(new ModuleLifecycleChangeRequest(coord, Actions.Delete));
    }
    try {
      moduleManager.prepare(group).commit().toCompletableFuture().get();
      console.errorln("Successfully removed plugins");
      return Result.success();
    } catch (Exception ex) {
      console.errorln("Failed to remove all plugins. Reason: %s", ex.getMessage());
      return Result.failure();
    }
  }

  private static class PluginRemovalListener implements EventListener<Object> {

    public PluginRemovalListener(Console console) {}

    @Override
    public void onEvent(EventType type, Event<Object> event) {}
  }
}
