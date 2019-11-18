package io.zephyr.kernel.shell;

import io.zephyr.kernel.shell.commands.ExitCommand;
import io.zephyr.kernel.shell.commands.KernelCommandSet;
import io.zephyr.kernel.shell.commands.PluginCommand;

public class KernelLauncherDecorator implements LauncherDecorator {
  @Override
  public void decorate(LauncherContext context) {
    context.getRegistry().register("exit", new ExitCommand());
    context.getRegistry().register("kernel", new KernelCommandSet());
    context.getRegistry().register("plugin", new PluginCommand());
  }
}
