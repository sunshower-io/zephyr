package io.sunshower.kernel;

public interface EventDispatcher<L, E> {
  void addEventListener(L listener);

  boolean removeEventListener(L listener);

  void dispatchEvent(E event);
}
