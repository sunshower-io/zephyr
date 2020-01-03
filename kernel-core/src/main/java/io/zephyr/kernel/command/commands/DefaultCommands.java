package io.zephyr.kernel.command.commands;

import io.zephyr.cli.CommandRegistry;
import io.zephyr.cli.CommandRegistryDecorator;
import io.zephyr.kernel.command.commands.kernel.KernelCommandSet;
import io.zephyr.kernel.command.commands.misc.HistoryCommand;
import io.zephyr.kernel.command.commands.plugin.PluginGroup;
import io.zephyr.kernel.command.commands.server.ServerCommandSet;

public class DefaultCommands implements CommandRegistryDecorator {

  @Override
  public void decorate(CommandRegistry registry) {
    registry.register(new ServerCommandSet());
    registry.register(new KernelCommandSet());
    registry.register(new HistoryCommand());
    registry.register(new PluginGroup());
  }
}
