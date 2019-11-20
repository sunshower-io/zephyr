package io.zephyr.kernel.command.commands.plugin;

import io.zephyr.kernel.command.DefaultCommand;
import picocli.CommandLine;

@CommandLine.Command(
    name = "plugin",
    subcommands = {InstallPluginCommand.class})
public class PluginInstallationGroup extends DefaultCommand {
  public PluginInstallationGroup() {
    super("plugin");
  }
}
