package io.zephyr.api;

import io.zephyr.kernel.events.EventType;
import lombok.Getter;

public enum ServiceEvents implements EventType {
  REGISTERED,
  UNREGISTERED;

  @Getter private final int id;

  ServiceEvents() {
    id = EventType.newId();
  }

  @Override
  public int getId() {
    return id;
  }
}
