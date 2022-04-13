package io.zephyr.kernel.core;

import io.zephyr.api.ServiceDefinition;
import java.util.function.Supplier;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class FactoryServiceDefinition<T> implements ServiceDefinition<T> {
  private final Class<T> type;
  private final String name;
  private final Supplier<T> factory;

  public FactoryServiceDefinition(Class<T> type, String name, Supplier<T> factory) {
    this.type = type;
    this.name = name;
    this.factory = factory;
  }

  public FactoryServiceDefinition(Class<T> type, Supplier<T> factory) {
    this(type, type.getName(), factory);
  }

  @Override
  public T get() {
    return factory.get();
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
