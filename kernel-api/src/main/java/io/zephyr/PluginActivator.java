package io.zephyr;

public interface PluginActivator {
  void start(PluginContext context);

  void stop(PluginContext context);
}
