package io.zephyr.common.io;

@FunctionalInterface
public interface Checker<T> {
  boolean check(T value);
}
