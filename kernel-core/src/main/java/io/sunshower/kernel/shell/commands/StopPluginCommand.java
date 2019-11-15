package io.sunshower.kernel.shell.commands;

import io.sunshower.kernel.launch.KernelLauncher;
import io.sunshower.kernel.module.ModuleLifecycle;
import picocli.CommandLine;

@CommandLine.Command(name = "stop")
public class StopPluginCommand extends PluginLifecycleCommand {
  @CommandLine.Parameters(index = "0..*")
  private String args[];

  @Override
  protected int execute() {
    apply(KernelLauncher.getKernel(), args, ModuleLifecycle.Actions.Stop);
    return 0;
  }
}
