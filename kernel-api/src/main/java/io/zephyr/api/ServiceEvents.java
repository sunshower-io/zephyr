package io.zephyr.api;

import io.sunshower.lang.events.EventType;

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
