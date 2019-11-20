package io.zephyr.kernel.command;

import io.zephyr.api.CommandContext;

import java.util.HashMap;
import java.util.Map;

public class DefaultCommandContext implements CommandContext {

  final Map<Class<?>, Object> services;

  public DefaultCommandContext() {
    services = new HashMap<>();
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getService(Class<T> service) {
    return (T) services.get(service);
  }


  public <T> void register(Class<T> service, T value) {
    services.put(service, value);
  }
}
