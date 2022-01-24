package io.zephyr.kernel.events;

import java.util.List;

public interface EventSource {

  int getListenerCount();

  boolean listensFor(EventType... types);

  <T> void addEventListener(EventListener<T> listener, EventType... types);

  <T> void addEventListener(EventListener<T> listener, int options, EventType... types);

  <T> void removeEventListener(EventListener<T> listener);

  <T> void dispatchEvent(EventType type, Event<T> event);

  List<EventListener<?>> getListeners();
}
