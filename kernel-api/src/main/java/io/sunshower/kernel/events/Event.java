package io.sunshower.kernel.events;

public interface Event<E, T> {
  E getType();
}
