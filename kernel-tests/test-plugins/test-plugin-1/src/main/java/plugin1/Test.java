package plugin1;

import io.zephyr.PluginActivator;
import io.zephyr.PluginContext;

public class Test implements PluginActivator {
  @Override
  public void start(PluginContext context) {
    System.out.println("Plugin1 starting...");
  }

  @Override
  public void stop(PluginContext context) {
    System.out.println("Plugin1 stopping...");
  }
}
