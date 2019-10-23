package io.sunshower.kernel.events;

public interface EventListener<E, T> {
  void onEvent(Event<E, T> event);
}
