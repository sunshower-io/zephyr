package io.zephyr.api;

import java.net.URL;
import java.util.Collection;

public interface Zephyr {

  void install(Collection<URL> urls);

  void start(Collection<String> pluginCoords);

  void stop(Collection<String> pluginCoords);

  void start(String...pluginCoords);
}
