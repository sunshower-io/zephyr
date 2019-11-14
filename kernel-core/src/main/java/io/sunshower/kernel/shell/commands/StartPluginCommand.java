package io.sunshower.kernel.shell.commands;

import io.sunshower.kernel.launch.KernelLauncher;
import io.sunshower.kernel.module.ModuleLifecycle;
import picocli.CommandLine;

@CommandLine.Command(name = "start")
public class StartPluginCommand extends PluginLifecycleCommand implements Runnable {
  @CommandLine.Parameters(index = "0..*")
  private String args[];

  @Override
  public void run() {
    apply(KernelLauncher.getKernel(), args, ModuleLifecycle.Actions.Activate);
  }
}
