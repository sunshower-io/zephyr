package io.zephyr.api;

public interface Disposable extends AutoCloseable {

  default void close() {
    dispose();
  }

  void dispose();
}
