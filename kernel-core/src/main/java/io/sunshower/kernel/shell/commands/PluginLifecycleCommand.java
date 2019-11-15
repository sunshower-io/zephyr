package io.sunshower.kernel.shell.commands;

import io.sunshower.kernel.core.Kernel;
import io.sunshower.kernel.core.ModuleCoordinate;
import io.sunshower.kernel.module.ModuleLifecycle;
import io.sunshower.kernel.module.ModuleLifecycleChangeGroup;
import io.sunshower.kernel.module.ModuleLifecycleChangeRequest;
import io.sunshower.kernel.shell.Command;
import lombok.val;

public class PluginLifecycleCommand extends Command {

  protected void apply(Kernel kernel, String[] plugins, ModuleLifecycle.Actions actions) {
    val tostart = new ModuleLifecycleChangeGroup();
    for (val c : plugins) {
      val coordinate = ModuleCoordinate.parse(c);
      val req = new ModuleLifecycleChangeRequest(coordinate, actions);
      tostart.addRequest(req);
    }

    try {
      kernel.getModuleManager().prepare(tostart).commit().toCompletableFuture().get();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
