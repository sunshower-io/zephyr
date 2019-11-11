package plugin1;

import io.sunshower.PluginActivator;
import io.sunshower.PluginContext;

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
