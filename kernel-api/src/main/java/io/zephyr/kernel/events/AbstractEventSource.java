package io.zephyr.kernel.events;

import java.util.*;
import lombok.AllArgsConstructor;
import lombok.val;

public class AbstractEventSource implements EventSource {
  final Map<BitSet, List<FlaggedEventListener>> listeners;

  protected AbstractEventSource() {
    listeners = new HashMap<>(2);
  }

  @Override
  public int getListenerCount() {
    synchronized (listeners) {
      int count = 0;
      for (val entry : listeners.values()) {
        count += entry.size();
      }
      return count;
    }
  }

  @Override
  public boolean listensFor(EventType... types) {
    synchronized (listeners) {
      return listeners.containsKey(createKey(types));
    }
  }

  @Override
  public <T> void addEventListener(
      io.zephyr.kernel.events.EventListener<T> listener, EventType... types) {
    addEventListener(listener, EventListener.Options.NONE, types);
  }

  @Override
  public <T> void addEventListener(EventListener<T> listener, int options, EventType... types) {
    synchronized (listeners) {
      BitSet bitset = createKey(types);

      List<FlaggedEventListener> results = listeners.get(bitset);
      if (results == null) {
        results = new ArrayList<>(1);
        listeners.put(bitset, results);
      }
      results.add(new FlaggedEventListener(options, listener));
    }
  }

  private BitSet createKey(EventType[] types) {
    val bitset = new BitSet();
    for (val type : types) {
      bitset.set(type.getId());
    }
    return bitset;
  }

  @Override
  public <T> void removeEventListener(EventListener<T> listener) {
    synchronized (listeners) {
      val listenerIter = listeners.entrySet().iterator();
      while (listenerIter.hasNext()) {
        val entry = listenerIter.next();
        val e = entry.getValue();
        val iter = e.iterator();
        while (iter.hasNext()) {
          val next = iter.next();
          if (next.listener == listener) {
            iter.remove();
          }
        }
        if (e.isEmpty()) {
          listenerIter.remove();
        }
      }
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> void dispatchEvent(EventType type, Event<T> event) {
    synchronized (listeners) {
      val entryIter = listeners.entrySet().iterator();
      while (entryIter.hasNext()) {
        val listeners = entryIter.next();
        val key = listeners.getKey();
        if (key.get(type.getId())) {
          val ls = listeners.getValue();
          val iter = ls.iterator();
          while (iter.hasNext()) {
            val next = iter.next();
            next.listener.onEvent(type, (Event) event);
            if (EventListener.Options.isSet(
                next.flags, EventListener.Options.REMOVE_AFTER_DISPATCH)) {
              iter.remove();
            }
          }
          if (ls.isEmpty()) {
            entryIter.remove();
          }
        }
      }
    }
  }

  @AllArgsConstructor
  static final class FlaggedEventListener {
    final int flags;
    final EventListener<?> listener;
  }
}
