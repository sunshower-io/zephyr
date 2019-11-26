package io.zephyr.kernel.memento;

public interface Originator {

  Memento save();

  void restore(Memento memento);
}
