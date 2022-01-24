package testproject2;

import io.zephyr.api.ModuleActivator;
import io.zephyr.api.ModuleContext;
import io.zephyr.api.ModuleEvents;
import io.zephyr.api.ModuleTracker;
import io.zephyr.api.ServiceRegistration;
import lombok.val;
import plugin1.Service;

public class Test implements ModuleActivator {

  private ServiceRegistration<String> registration;
  private ModuleTracker moduleTracker;
  private ServiceRegistration<Service> registration2;

  public Test() {
    val plugin = new plugin1.Test();
    System.out.println(plugin);
  }

  @Override
  public void start(ModuleContext context) {

    moduleTracker = context.trackModules(t -> true);
    moduleTracker.addEventListener(
        (type, event) -> System.out.format("%s: %s", type, event.getTarget()),
        ModuleEvents.INSTALLED);
    registration = context.register(String.class, "hello");
    registration2 = context.register(Service.class, new PluginService2());
    System.out.println("Plugin2 starting...");
    context
        .getReferences(Service.class)
        .forEach(service -> service.getDefinition().get().sayHello());
  }

  @Override
  public void stop(ModuleContext context) {
    registration.dispose();
    registration2.dispose();
    moduleTracker.close();
    System.out.println("plugin2 stopping...");
  }
}
