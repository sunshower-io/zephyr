package testproject2;

import io.zephyr.api.ModuleActivator;
import io.zephyr.api.ModuleContext;
import io.zephyr.api.ModuleEvents;
import io.zephyr.api.ModuleTracker;
import io.zephyr.api.ServiceRegistration;

public class Test implements ModuleActivator {

  private ServiceRegistration<String> registration;
  private ModuleTracker moduleTracker;

  public Test() {}

  @Override
  public void start(ModuleContext context) {

    moduleTracker = context.trackModules(t -> true);
    moduleTracker.addEventListener(
        (type, event) -> System.out.format("%s: %s", type, event.getTarget()),
        ModuleEvents.INSTALLED);
    registration = context.register(String.class, "hello");
    System.out.println("Plugin2 starting...");
  }

  @Override
  public void stop(ModuleContext context) {
    registration.dispose();
    moduleTracker.close();
    System.out.println("plugin2 stopping...");
  }
}
