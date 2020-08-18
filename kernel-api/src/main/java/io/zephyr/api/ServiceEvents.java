package io.zephyr.api;

import io.zephyr.kernel.events.EventType;

public enum ServiceEvents implements EventType {
  REGISTERED,
  UNREGISTERED;

  private final int id;

  ServiceEvents() {
    id = EventType.newId();
  }

  @Override
  public int getId() {
    return id;
  }
}
