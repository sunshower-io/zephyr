package io.zephyr.kernel.events;

import io.zephyr.kernel.status.Status;

public interface Event<T> {
  T getTarget();
  Status getStatus();
}
