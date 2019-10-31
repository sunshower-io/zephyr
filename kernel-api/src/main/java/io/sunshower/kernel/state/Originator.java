package io.sunshower.kernel.state;

public interface Originator<T> {

  Memento<T> save(T value);

  void restore(Memento<T> memento);
}
