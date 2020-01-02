package testproject2;

import io.zephyr.PluginActivator;
import io.zephyr.PluginContext;
import io.zephyr.kernel.Module;
import lombok.val;

public class Test implements PluginActivator {
  public Test() {
    val plugin = new plugin1.Test();
    System.out.println(plugin);
  }

  @Override
  public void start(PluginContext context, Module md) {
    System.out.println("Plugin2 starting...");
  }

  @Override
  public void stop(PluginContext context, Module md) {
    System.out.println("plugin2 stopping...");
  }
}
