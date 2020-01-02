package plugin1;

import io.zephyr.PluginActivator;
import io.zephyr.PluginContext;
import io.zephyr.kernel.Module;

public class Test implements PluginActivator {
  @Override
  public void start(PluginContext context, Module md) {
    System.out.println("Plugin1 starting...");
  }

  @Override
  public void stop(PluginContext context, Module md) {
    System.out.println("Plugin1 stopping...");
  }
}
