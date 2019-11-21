package io.zephyr.kernel.command.commands.plugin;

import io.zephyr.api.CommandContext;
import io.zephyr.api.Result;
import io.zephyr.kernel.module.ModuleLifecycle;
import picocli.CommandLine;

@CommandLine.Command(name = StartPluginCommand.name)
public class StartPluginCommand extends PluginLifecycleCommand {
  static final String name = "start";
  private static final long serialVersionUID = -7036457295462146170L;

  @CommandLine.Parameters String[] plugins;

  public StartPluginCommand() {
    super(name);
  }

  @Override
  public Result execute(CommandContext context) {
    return execute(context, ModuleLifecycle.Actions.Activate, plugins);
  }
}
