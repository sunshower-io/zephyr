package io.sunshower.kernel.state;

public interface Caretaker {
  <T> void save(T t);

  <T> T restore(Class<T> type);
}
