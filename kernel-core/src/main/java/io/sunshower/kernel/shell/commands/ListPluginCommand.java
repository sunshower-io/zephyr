package io.sunshower.kernel.shell.commands;

import io.sunshower.kernel.launch.KernelLauncher;
import lombok.val;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "list")
public class ListPluginCommand implements Callable<Void> {

  @Override
  public Void call() throws Exception {
    val kernel = KernelLauncher.getKernel();
    val cmd = KernelLauncher.getConsole();
    if (kernel == null) {
      cmd.format("Kernel is not running (have you called 'kernel start'?)");
      return null;
    }
    cmd.format("Installed Plugins:\n");
    val modules = kernel.getModuleManager().getModules();
    for (val module : modules) {
      cmd.format(
          "\t%s | state: %s\n",
          module.getCoordinate().toCanonicalForm(), module.getLifecycle().getState());
    }

    return null;
  }
}
