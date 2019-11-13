package io.sunshower.kernel.shell;

import io.sunshower.kernel.shell.commands.ExitCommand;
import io.sunshower.kernel.shell.commands.PluginCommand;
import io.sunshower.kernel.shell.commands.KernelCommandSet;

public class KernelLauncherDecorator implements LauncherDecorator {
  @Override
  public void decorate(LauncherContext context) {
    context.getRegistry().register("exit", new ExitCommand());
    context.getRegistry().register("kernel", new KernelCommandSet(context));
    context.getRegistry().register("plugin", new PluginCommand());
  }
}
