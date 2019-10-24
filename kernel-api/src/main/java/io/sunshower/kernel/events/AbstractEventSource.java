package io.sunshower.kernel.events;

import java.util.*;
import lombok.NonNull;
import lombok.val;

public class AbstractEventSource<E, T> implements EventSource<E, T> {

  private final Class<? extends E> type;
  private final Object lock = new Object();

  private final Map<E, List<EventListener<E, T>>> listeners;

  protected AbstractEventSource(@NonNull Class<? extends E> type) {
    this.type = type;
    this.listeners = create(type);
  }

  @Override
  public boolean handles(@NonNull Class type) {
    return type.equals(this.type);
  }

  @Override
  public void dispatch(@NonNull Event<E, T> event) {
    synchronized (lock) {
      val type = event.getType();
      val subset = listeners.get(type);
      if (subset != null) {
        for (val listener : subset) {
          listener.onEvent(event);
        }
      }
    }
  }

  @Override
  public void removeListener(@NonNull E type, @NonNull EventListener<E, T> listener) {
    synchronized (lock) {
      val subset = listeners.get(type);
      if (subset != null) {
        subset.remove(listener);
      }
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public void registerListener(@NonNull E type, @NonNull EventListener<E, T> listener) {
    synchronized (lock) {
      if (handles(type.getClass())) {
        listeners.computeIfAbsent(type, k -> new ArrayList<>()).add(listener);
      }
    }
  }

  @SuppressWarnings("unchecked")
  protected Map<E, List<EventListener<E, T>>> create(Class<? extends E> type) {
    if (Enum.class.isAssignableFrom(type)) {
      return new EnumMap(type);
    } else {
      return new HashMap<>();
    }
  }
}
