package io.zephyr.api;

public interface Disposable extends AutoCloseable {

  default void close() {
    dispose();
  }

  /** close this and release any resources associated with it */
  void dispose();
}
