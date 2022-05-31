package io.zephyr.kernel.modules.shell.command.commands.plugin;

import io.zephyr.kernel.modules.shell.command.DefaultCommand;
import picocli.CommandLine;

@CommandLine.Command(
    name = "plugin",
    subcommands = {
      InstallPluginCommand.class,
      ListPluginCommand.class,
      StartPluginCommand.class,
      StopPluginCommand.class,
      RemovePluginCommand.class
    })
public class PluginGroup extends DefaultCommand {

  private static final long serialVersionUID = -7605771110363732824L;

  public PluginGroup() {
    super("plugin");
  }
}
