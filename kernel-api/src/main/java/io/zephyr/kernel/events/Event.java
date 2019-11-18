package io.zephyr.kernel.events;

public interface Event<T> {
  T getTarget();
}
