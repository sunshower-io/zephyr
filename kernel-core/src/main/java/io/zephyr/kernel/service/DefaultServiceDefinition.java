package io.zephyr.kernel.service;

import io.zephyr.api.ServiceDefinition;

public class DefaultServiceDefinition<T> implements ServiceDefinition<T> {
  final T value;
  final String name;
  final Class<T> type;

  public DefaultServiceDefinition(final Class<T> type, final T value) {
    this(type, type.toString(), value);
  }

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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof DefaultServiceDefinition)) return false;

    DefaultServiceDefinition<?> that = (DefaultServiceDefinition<?>) o;

    if (!value.equals(that.value)) return false;
    if (!name.equals(that.name)) return false;
    return type.equals(that.type);
  }

  @Override
  public int hashCode() {
    int result = value.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + type.hashCode();
    return result;
  }
}
