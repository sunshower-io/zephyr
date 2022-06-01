package io.zephyr.kernel.modules.shell.command.commands;

import io.zephyr.kernel.modules.shell.command.commands.kernel.KernelCommandSet;
import io.zephyr.kernel.modules.shell.command.commands.misc.HistoryCommand;
import io.zephyr.kernel.modules.shell.command.commands.plugin.PluginGroup;
import io.zephyr.kernel.modules.shell.command.commands.plugin.RemovePluginCommand;
import io.zephyr.kernel.modules.shell.command.commands.server.ServerCommandSet;
import io.zephyr.kernel.modules.shell.console.CommandRegistry;
import io.zephyr.kernel.modules.shell.console.CommandRegistryDecorator;

public class DefaultCommands implements CommandRegistryDecorator {

  @Override
  public void decorate(CommandRegistry registry) {
    registry.register(new ServerCommandSet());
    registry.register(new KernelCommandSet());
    registry.register(new HistoryCommand());
    registry.register(new PluginGroup());
    registry.register(new RemovePluginCommand());
  }
}
