package io.zephyr.api;

public interface ServiceDefinition<T> {
  T get();

  String getName();

  Class<T> getType();
}
