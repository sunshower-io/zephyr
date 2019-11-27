package io.zephyr.api;

import io.zephyr.kernel.Coordinate;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.core.ModuleCoordinate;
import io.zephyr.kernel.module.*;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import lombok.val;

@SuppressWarnings({
  "PMD.BeanMembersShouldSerialize",
  "PMD.AvoidDuplicateLiterals",
  "PMD.DataflowAnomalyAnalysis"
})
public class DefaultZephyr implements Zephyr {
  static final Logger log = Logger.getLogger(Zephyr.class.getName());
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
      log.log(Level.INFO, "Operation failed:", ex.getMessage());
    }
  }

  @Override
  public void start(Collection<String> pluginCoords) {
    try {
      changeLifecycle(pluginCoords, ModuleLifecycle.Actions.Activate);
    } catch (Exception ex) {
      ex.printStackTrace();
      log.log(Level.INFO, "Operation failed:", ex.getMessage());
    }
  }

  @Override
  public void stop(Collection<String> pluginCoords) {
    try {
      changeLifecycle(pluginCoords, ModuleLifecycle.Actions.Stop);
    } catch (Exception ex) {
      log.log(Level.INFO, "Operation failed:", ex.getMessage());
    }
  }

  @Override
  public void remove(Collection<String> pluginCoords) {
    try {
      changeLifecycle(pluginCoords, ModuleLifecycle.Actions.Delete);
    } catch (Exception ex) {
      log.log(Level.INFO, "Operation failed:", ex.getMessage());
    }
  }

  @Override
  public List<Module> getPlugins() {
    return Collections.unmodifiableList(kernel.getModuleManager().getModules());
  }

  @Override
  public List<Module> getPlugins(ModuleLifecycle.State state) {
    return Collections.unmodifiableList(kernel.getModuleManager().getModules(state));
  }

  @Override
  public List<Coordinate> getPluginCoordinates() {
    return getPlugins().stream().map(Module::getCoordinate).collect(Collectors.toList());
  }

  @Override
  public List<Coordinate> getPluginCoordinates(ModuleLifecycle.State state) {
    return getPlugins(state).stream().map(Module::getCoordinate).collect(Collectors.toList());
  }

  @Override
  public void remove(String... pluginCoords) {
    remove(Arrays.asList(pluginCoords));
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
      log.log(Level.INFO, ex.getMessage());
    }
  }

  @Override
  public void startup() {
    try {
      kernel.start();
      kernel.restoreState().toCompletableFuture().get();
    } catch (Exception ex) {
      log.log(Level.INFO, ex.getMessage());
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
