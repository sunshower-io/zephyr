package io.zephyr.common.io;

public interface Checker<T> {
  String name();

  boolean check(T value);
}
