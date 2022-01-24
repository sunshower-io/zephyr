package io.zephyr.kernel.service;

import io.zephyr.api.ServiceDefinition;
import io.zephyr.api.ServiceReference;
import io.zephyr.api.ServiceRegistration;

public class DefaultServiceRegistration<T> implements ServiceRegistration<T> {
  final ServiceReference<T> reference;
  final ModuleServiceRegistry registry;
  final ServiceDefinition<T> definition;

  public DefaultServiceRegistration(
      ServiceReference<T> reference,
      ModuleServiceRegistry registry,
      ServiceDefinition<T> definition) {
    this.registry = registry;
    this.reference = reference;
    this.definition = definition;
  }

  @Override
  public ServiceReference<T> getReference() {
    return reference;
  }

  @Override
  public <S> boolean provides(Class<S> type) {
    return definition.getType().isAssignableFrom(type);
  }

  @Override
  public void dispose() {
    registry.unregister(this);
  }
}
