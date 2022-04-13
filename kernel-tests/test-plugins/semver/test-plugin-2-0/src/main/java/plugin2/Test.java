package plugin2;

import io.zephyr.api.*;
import java.util.Objects;

public class Test implements ModuleActivator {
  private ModuleTracker moduleTracker;
  private ServiceRegistration<Service> registration;
  private ServiceTracker serviceTracker;

  @Override
  public void start(ModuleContext context) {
    System.out.println("Plugin2 starting...");
    moduleTracker = context.trackModules(t -> true);
    moduleTracker.addEventListener(
        (type, event) -> System.out.format("%s: %s", type, event.getTarget()),
        ModuleEvents.INSTALLED);
    serviceTracker = context.trackServices(t -> !Objects.equals(t.getModule(), context.getModule()));
    serviceTracker.addEventListener((t, u) -> {
      registration = context.register(Service.class, new Plugin2Service(((ServiceReference<?>) u.getTarget()).getDefinition().get()));


    }, ServiceEvents.REGISTERED);
    System.out.println("Plugin2 started...");
  }

  @Override
  public void stop(ModuleContext context) {
    System.out.println("Plugin2 stopping...");
    moduleTracker.close();
    registration.close();
    serviceTracker.close();
    System.out.println("Plugin2 stopped");
  }
}
