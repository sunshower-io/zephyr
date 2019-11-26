package io.zephyr.api;

import io.zephyr.kernel.Module;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.core.ModuleCoordinate;
import io.zephyr.kernel.module.*;
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
  public void install(URL... urls) {

    val installationGroup = new ModuleInstallationGroup();
    for (val url : urls) {
      val request = new ModuleInstallationRequest();
      request.setLocation(url);
      installationGroup.add(request);
    }
    try {
      kernel.getModuleManager().prepare(installationGroup).commit().toCompletableFuture().get();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

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

  @Override
  public void shutdown() {
    try {
      kernel.persistState().toCompletableFuture().get();
      kernel.stop();
    } catch (Exception ex) {
      throw new RuntimeException(ex); // // TODO: 11/25/19 create a better exception
    }
  }

  @Override
  public void startup() {
    try {
      kernel.start();
      kernel.restoreState().toCompletableFuture().get();
    } catch (Exception ex) {
      throw new RuntimeException(ex); // // TODO: 11/25/19 create a better exception
    }
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
