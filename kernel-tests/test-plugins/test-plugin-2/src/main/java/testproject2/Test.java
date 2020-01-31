package testproject2;

import io.zephyr.api.ModuleActivator;
import io.zephyr.api.ModuleContext;
import lombok.val;

public class Test implements ModuleActivator {
  public Test() {
    val plugin = new plugin1.Test();
    System.out.println(plugin);
  }

  @Override
  public void start(ModuleContext context) {
    System.out.println("Plugin2 starting...");
  }

  @Override
  public void stop(ModuleContext context) {
    System.out.println("plugin2 stopping...");
  }
}
