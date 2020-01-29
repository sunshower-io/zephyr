package io.zephyr.kernel.service;

import io.zephyr.api.ServiceDefinition;

public class DefaultServiceDefinition<T> implements ServiceDefinition<T> {
  final T value;
  final String name;
  final Class<T> type;

  public DefaultServiceDefinition(final Class<T> type, final String name, final T value) {
    this.name = name;
    this.type = type;
    this.value = value;
  }

  @Override
  public T get() {
    return value;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Class<T> getType() {
    return type;
  }
}
