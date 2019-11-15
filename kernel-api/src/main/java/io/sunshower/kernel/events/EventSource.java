package io.sunshower.kernel.events;

public interface EventSource {

  <T> void addEventListener(EventListener<T> listener, EventType... types);

  <T> void removeEventListener(EventListener<T> listener);

  <T> void dispatchEvent(EventType type, Event<T> event);
}
