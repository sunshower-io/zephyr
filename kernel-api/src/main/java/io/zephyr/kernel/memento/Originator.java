package io.zephyr.kernel.memento;

public interface Originator<T> {

  Memento<T> save();

  void restore(Memento<T> memento);
}
