package io.zephyr.kernel.modules.shell.command;

import io.zephyr.kernel.extensions.EntryPoint;
import io.zephyr.kernel.modules.shell.console.CommandContext;
import java.util.HashMap;
import java.util.Map;

public class DefaultCommandContext implements CommandContext {

  final Map<Class<?>, Object> services;

  final Object lock = new Object();
  private final Map<EntryPoint.ContextEntries, Object> launchContext;

  public DefaultCommandContext(Map<EntryPoint.ContextEntries, Object> context) {
    services = new HashMap<>();
    this.launchContext = context;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getService(Class<T> service) {
    synchronized (lock) {
      return (T) services.get(service);
    }
  }

  @Override
  public Map<EntryPoint.ContextEntries, Object> getLaunchContext() {
    return launchContext;
  }

  public <T> void register(Class<T> service, T value) {
    synchronized (lock) {
      services.put(service, value);
    }
  }
}
