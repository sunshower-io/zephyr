package io.zephyr.api;

import io.zephyr.kernel.Module;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.core.ModuleCoordinate;
import io.zephyr.kernel.module.ModuleLifecycle;
import io.zephyr.kernel.module.ModuleLifecycleChangeGroup;
import io.zephyr.kernel.module.ModuleLifecycleChangeRequest;
import lombok.val;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

public class DefaultZephyr implements Zephyr {
  /** The module that this zephyr instance is called from */
  private final Module module;

  /** the underlying kernel instance */
  private final Kernel kernel;

  public DefaultZephyr(final Module module, final Kernel kernel) {
    this.module = module;
    this.kernel = kernel;
  }

  @Override
  public void install(Collection<URL> urls) {}

  @Override
  public void start(Collection<String> pluginCoords) {
    try {
      changeLifecycle(pluginCoords, ModuleLifecycle.Actions.Activate);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  @Override
  public void stop(Collection<String> pluginCoords) {
    try {
      changeLifecycle(pluginCoords, ModuleLifecycle.Actions.Stop);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  @Override
  public void start(String... pluginCoords) {
    start(Arrays.asList(pluginCoords));
  }

  private void changeLifecycle(Collection<String> pluginCoords, ModuleLifecycle.Actions action)
      throws ExecutionException, InterruptedException {
    val group = new ModuleLifecycleChangeGroup();
    for (val coord : pluginCoords) {
      val req = new ModuleLifecycleChangeRequest(ModuleCoordinate.parse(coord), action);
      group.addRequest(req);
    }
    kernel.getModuleManager().prepare(group).commit().toCompletableFuture().get();
  }
}
