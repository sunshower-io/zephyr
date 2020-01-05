package io.zephyr.kernel.events;

import java.util.concurrent.atomic.AtomicInteger;

public interface EventType {

  default int getId() {
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
