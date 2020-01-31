package testproject2;

import io.zephyr.api.ModuleActivator;
import io.zephyr.api.ModuleContext;
import io.zephyr.api.ServiceRegistration;
import lombok.val;

public class Test implements ModuleActivator {
  private ServiceRegistration<String> registration;

  public Test() {
    val plugin = new plugin1.Test();
    System.out.println(plugin);
  }

  @Override
  public void start(ModuleContext context) {
    registration = context.register(String.class, "hello");
    System.out.println("Plugin2 starting...");
  }

  @Override
  public void stop(ModuleContext context) {
    registration.dispose();
    System.out.println("plugin2 stopping...");
  }
}
