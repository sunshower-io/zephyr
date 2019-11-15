package io.sunshower.kernel.events;

public interface EventListener<T> {

  void onEvent(EventType type, Event<T> event);
}
