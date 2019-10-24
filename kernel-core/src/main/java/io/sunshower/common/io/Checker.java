package io.sunshower.common.io;

@FunctionalInterface
public interface Checker<T> {
  boolean check(T value);
}
