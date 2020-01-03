package io.zephyr.kernel.command;

import io.zephyr.cli.CommandContext;
import java.util.HashMap;
import java.util.Map;

public class DefaultCommandContext implements CommandContext {

  final Map<Class<?>, Object> services;

  final Object lock = new Object();

  public DefaultCommandContext() {
    services = new HashMap<>();
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getService(Class<T> service) {
    synchronized (lock) {
      return (T) services.get(service);
    }
  }

  public <T> void register(Class<T> service, T value) {
    synchronized (lock) {
      services.put(service, value);
    }
  }
}
