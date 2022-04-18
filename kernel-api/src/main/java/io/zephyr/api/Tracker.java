package io.zephyr.api;

import io.sunshower.lang.events.EventSource;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public interface Tracker<T> extends EventSource, AutoCloseable, Startable, Stoppable {

  /** close this tracker and dispose of any resources/listeners it has acquired or created */
  void close();

  /**
   * stop this tracker. This operation may be undone by open() if this tracker has not been closed
   */
  void stop();

  List<T> getTracked();

  int getTrackedCount();

  void waitUntil(Predicate<? super Collection<T>> condition);
}
