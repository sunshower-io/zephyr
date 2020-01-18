package io.zephyr.kernel.events;

import java.util.concurrent.atomic.AtomicInteger;

public interface EventType {

  int getId();

  static int newId() {
    return instance.id();
  }

  Generator instance = new Generator();

  final class Generator {
    static final AtomicInteger counter = new AtomicInteger();

    final int id() {
      return counter.getAndIncrement();
    }
  }
}
