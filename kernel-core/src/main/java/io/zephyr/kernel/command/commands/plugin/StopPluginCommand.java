package io.zephyr.kernel.command.commands.plugin;

import io.zephyr.api.CommandContext;
import io.zephyr.api.Result;
import io.zephyr.kernel.module.ModuleLifecycle;
import picocli.CommandLine;

@CommandLine.Command(name = StopPluginCommand.name)
public class StopPluginCommand extends PluginLifecycleCommand {
  static final String name = "stop";
  private static final long serialVersionUID = -1720281697624323485L;

  @CommandLine.Parameters String[] plugins;

  public StopPluginCommand() {
    super(name);
  }

  @Override
  public Result execute(CommandContext context) {
    return execute(context, ModuleLifecycle.Actions.Stop, plugins);
  }
}
