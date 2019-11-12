package io.sunshower.kernel.memento;

public interface Originator<T> {
  Memento<T> getMemento();
}
