package io.zephyr.kernel.shell.commands;

import io.zephyr.kernel.core.Kernel;
import io.sunshower.kernel.core.ModuleCoordinate;
import io.zephyr.kernel.module.ModuleLifecycle;
import io.zephyr.kernel.module.ModuleLifecycleChangeGroup;
import io.zephyr.kernel.module.ModuleLifecycleChangeRequest;
import io.zephyr.kernel.shell.Command;
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
