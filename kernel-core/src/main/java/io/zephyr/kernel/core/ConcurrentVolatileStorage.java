package io.zephyr.kernel.core;

import io.zephyr.kernel.VolatileStorage;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
final class ConcurrentVolatileStorage implements VolatileStorage {

  final Map<Object, Object> storage;

  ConcurrentVolatileStorage() {
    storage = new HashMap<>();
  }

  @Override
  public <K, V> V get(K key) {
    synchronized (storage) {
      return (V) storage.get(key);
    }
  }

  @Override
  public <K, V> V set(K key, V value) {
    synchronized (storage) {
      return (V) storage.put(key, value);
    }
  }

  @Override
  public <K> boolean contains(K key) {
    synchronized (storage) {
      return storage.containsKey(key);
    }
  }

  @Override
  public void clear() {
    storage.clear();
  }

  @Override
  public void start() {
    clear();
  }

  @Override
  public void stop() {
    clear();
  }
}
