package io.zephyr.kernel.memento;

public interface Originator<T> {
  Memento<T> getMemento();
}
