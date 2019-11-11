package io.sunshower;

public interface PluginActivator {

  void start(PluginContext context);

  void stop(PluginContext context);
}
