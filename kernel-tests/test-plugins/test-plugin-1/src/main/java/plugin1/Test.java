package plugin1;

import io.zephyr.api.ModuleContext;
import io.zephyr.api.PluginActivator;

public class Test implements PluginActivator {
  @Override
  public void start(ModuleContext context) {
    System.out.println("Plugin1 starting...");
  }

  @Override
  public void stop(ModuleContext context) {
    System.out.println("Plugin1 stopping...");
  }
}
