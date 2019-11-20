package io.zephyr.kernel.command.commands.plugin;

import io.zephyr.kernel.command.DefaultCommand;
import picocli.CommandLine;

@CommandLine.Command(
    name = "plugin",
    subcommands = {InstallPluginCommand.class, ListPluginCommand.class})
public class PluginInstallationGroup extends DefaultCommand {
  private static final long serialVersionUID = -7605771110363732824L;

  public PluginInstallationGroup() {
    super("plugin");
  }
}
