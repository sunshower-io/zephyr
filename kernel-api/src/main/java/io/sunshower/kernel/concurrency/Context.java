package io.sunshower.kernel.concurrency;

import io.sunshower.gyre.Scope;

public interface Context extends Scope {
  /** Set a named value visible to this scope and its children */
  Object set(String key, Object value);

  <T> T get(String key);

  <T> T get(Class<T> type);

  <T> void push(T value);

  <T> T pop();

  int stackDepth();
}
