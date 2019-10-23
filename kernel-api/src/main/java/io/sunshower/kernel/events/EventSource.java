package io.sunshower.kernel.events;

public interface EventSource<E, T> {

  boolean handles(Class type);

  void dispatch(Event<E, T> event);

  void removeListener(E type, EventListener<E, T> listener);

  void registerListener(E type, EventListener<E, T> listener);
}
