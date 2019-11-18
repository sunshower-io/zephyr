package io.zephyr.kernel.shell.commands;

import io.zephyr.kernel.shell.Command;
import picocli.CommandLine;

@SuppressWarnings("PMD.DoNotUseThreads")
@CommandLine.Command(
  name = "plugin",
  subcommands = {
    PluginInstallCommand.class,
    ListPluginCommand.class,
    StartPluginCommand.class,
    StopPluginCommand.class
  }
)
public class PluginCommand extends Command {}
