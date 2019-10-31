package io.sunshower.kernel.state;

public interface Originator<T> {

  Memento<T> save();

  void restore(Memento<T> memento);
}
