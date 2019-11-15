package io.sunshower.kernel.shell.commands;

import io.sunshower.kernel.launch.KernelLauncher;
import java.util.concurrent.Callable;

import io.sunshower.kernel.shell.Command;
import lombok.val;
import picocli.CommandLine;

@CommandLine.Command(name = "list")
public class ListPluginCommand extends Command {

  @Override
  protected int execute() {
    val kernel = KernelLauncher.getKernel();
    val cmd = KernelLauncher.getConsole();
    if (kernel == null) {
      cmd.format("Kernel is not running (have you called 'kernel start'?)");
      return -1;
    }
    cmd.format("Installed Plugins:\n");
    val modules = kernel.getModuleManager().getModules();
    for (val module : modules) {
      cmd.format(
          "\t%s | state: %s\n",
          module.getCoordinate().toCanonicalForm(), module.getLifecycle().getState());
    }

    return 0;
  }
}
