package io.zephyr.api;

import io.zephyr.kernel.core.Kernel;

public interface ServiceRegistry extends AutoCloseable {

  void close();

  void initialize(Kernel kernel);

  Kernel getKernel();

  <T> ServiceRegistration<T> register(ServiceDefinition<T> definition);

  <T> void unregister(ServiceRegistration<T> definition);
}
