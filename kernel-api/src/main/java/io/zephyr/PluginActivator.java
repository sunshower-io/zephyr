package io.zephyr;

import io.zephyr.kernel.Module;

public interface PluginActivator {

  void start(PluginContext context, Module module);

  void stop(PluginContext context, Module module);
}
