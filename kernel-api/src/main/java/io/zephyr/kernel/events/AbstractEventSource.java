package io.zephyr.kernel.events;

import lombok.val;

import java.util.*;

public class AbstractEventSource implements EventSource {
  private final Map<BitSet, List<io.zephyr.kernel.events.EventListener<?>>> listeners;

  protected AbstractEventSource() {
    listeners = new HashMap<>(2);
  }

  @Override
  public <T> void addEventListener(io.zephyr.kernel.events.EventListener<T> listener, EventType... types) {
    synchronized (listeners) {
      val bitset = new BitSet();
      for (val type : types) {
        bitset.set(type.getId());
      }

      List<io.zephyr.kernel.events.EventListener<?>> results = listeners.get(bitset);
      if (results == null) {
        results = new ArrayList<>(1);
        listeners.put(bitset, results);
      }
      results.add(listener);
    }
  }

  @Override
  public <T> void removeEventListener(EventListener<T> listener) {
    synchronized (listeners) {
      for (val e : listeners.values()) {
        if (e.remove(listener)) {
          break;
        }
      }
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> void dispatchEvent(EventType type, Event<T> event) {
    synchronized (listeners) {
      for (val listeners : listeners.entrySet()) {
        val key = listeners.getKey();
        if (key.get(type.getId())) {
          val ls = listeners.getValue();
          for (val listener : ls) {
            listener.onEvent(type, (Event) event);
          }
        }
      }
    }
  }
}
