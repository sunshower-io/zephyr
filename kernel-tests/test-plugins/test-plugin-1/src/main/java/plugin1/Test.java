package plugin1;

import io.zephyr.api.ModuleContext;
import io.zephyr.api.ModuleEvents;
import io.zephyr.api.ModuleTracker;
import io.zephyr.api.PluginActivator;
import io.zephyr.kernel.events.Event;
import io.zephyr.kernel.events.EventListener;
import io.zephyr.kernel.events.EventType;

public class Test implements PluginActivator {
  private ModuleTracker moduleTracker;

  @Override
  public void start(ModuleContext context) {
    System.out.println("Plugin1 starting...");
    moduleTracker = context.trackModules(t -> true);
    moduleTracker.addEventListener(
        new EventListener<Object>() {
          @Override
          public void onEvent(EventType type, Event<Object> event) {
            System.out.println(type);
          }
        },
        ModuleEvents.INSTALLED);
  }

  @Override
  public void stop(ModuleContext context) {
    System.out.println("Plugin1 stopping...");
    moduleTracker.close();
  }
}
