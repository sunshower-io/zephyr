package io.zephyr.kernel.modules.shell.command.commands.plugin;

import io.zephyr.kernel.module.ModuleLifecycle;
import io.zephyr.kernel.modules.shell.console.CommandContext;
import io.zephyr.kernel.modules.shell.console.Result;
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
