package io.zephyr.kernel.command;

import io.zephyr.api.CommandContext;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.misc.SuppressFBWarnings;
import io.zephyr.kernel.server.Server;

import java.util.HashMap;
import java.util.Map;

public class DefaultCommandContext implements CommandContext {

  final Kernel kernel;
  final Map<Class<?>, Object> services;

  public DefaultCommandContext(Kernel kernel) {
    this.kernel = kernel;
    services = new HashMap<>();
    services.put(Kernel.class, kernel);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getService(Class<T> service) {
    return (T) services.get(service);
  }

  @Override
  public Kernel getKernel() {
    return kernel;
  }

  public <T> void register(Class<T> service, T value) {
    services.put(service, value);
  }
}
