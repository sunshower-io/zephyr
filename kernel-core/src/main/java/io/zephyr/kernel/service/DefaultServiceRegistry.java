package io.zephyr.kernel.service;

import io.zephyr.api.ServiceDefinition;
import io.zephyr.api.ServiceRegistration;
import io.zephyr.api.ServiceRegistry;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.log.Logging;
import lombok.NonNull;

import java.util.logging.Logger;

public class DefaultServiceRegistry implements ServiceRegistry {

  static final Logger log = Logging.get(ServiceRegistry.class);

  private Kernel kernel;

  @Override
  public void close() {}

  public void initialize(@NonNull Kernel kernel) {
    this.kernel = kernel;
  }

  @Override
  public Kernel getKernel() {
    return kernel;
  }

  @Override
  public <T> ServiceRegistration<T> register(ServiceDefinition<T> definition) {


  }

  @Override
  public <T> void unregister(ServiceRegistration<T> definition) {

  }
}
