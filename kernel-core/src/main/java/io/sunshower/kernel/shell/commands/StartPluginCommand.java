package io.sunshower.kernel.shell.commands;

import io.sunshower.kernel.core.ModuleCoordinate;
import io.sunshower.kernel.launch.KernelLauncher;
import io.sunshower.kernel.module.ModuleLifecycle;
import io.sunshower.kernel.module.ModuleLifecycleChangeGroup;
import io.sunshower.kernel.module.ModuleLifecycleChangeRequest;
import lombok.val;
import picocli.CommandLine;


@CommandLine.Command(name = "start")
public class StartPluginCommand implements Runnable {
  @CommandLine.Parameters(index = "0..*")
  private String args[];

  @Override
  public void run() {
    val tostart = new ModuleLifecycleChangeGroup();
    for (val c : args) {
      val coordinate = ModuleCoordinate.parse(c);
      val req = new ModuleLifecycleChangeRequest(coordinate, ModuleLifecycle.Actions.Activate);
      tostart.addRequest(req);
    }

    try {
      KernelLauncher.getKernel()
          .getModuleManager()
          .prepare(tostart)
          .commit()
          .toCompletableFuture()
          .get();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
