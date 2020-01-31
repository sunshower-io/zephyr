package io.zephyr.cli;

import io.zephyr.kernel.Coordinate;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.module.ModuleLifecycle;
import java.net.URL;
import java.util.Collection;
import java.util.List;

public interface Zephyr {

  void install(Collection<URL> urls);

  void install(URL... urls);

  void start(Collection<String> pluginCoords);

  void stop(Collection<String> pluginCoords);

  void remove(Collection<String> pluginCoords);

  List<Module> getPlugins();

  List<Module> getPlugins(ModuleLifecycle.State state);

  List<Coordinate> getPluginCoordinates();

  List<Coordinate> getPluginCoordinates(ModuleLifecycle.State state);

  void remove(String... pluginCoords);

  void start(String... pluginCoords);

  void shutdown();

  void startup();

  void restart();
}
