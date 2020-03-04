package io.zephyr.kernel;

import io.zephyr.api.Startable;
import io.zephyr.api.Stoppable;

public interface VolatileStorage extends Stoppable, Startable {

  <K, V> V get(K key);

  <K, V> V set(K key, V value);

  <K> boolean contains(K key);

  void clear();
}
