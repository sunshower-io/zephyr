package io.sunshower.kernel;

import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import lombok.val;

/**
 * @param <L> the type of the listener
 * @param <E> the type of the event processed
 */
public abstract class AbstractEventDispatcher<L, E> implements EventDispatcher<L, E> {

  private final List<L> listeners;

  protected AbstractEventDispatcher() {
    listeners = new ArrayList<>();
  }

  @Override
  public void addEventListener(@NonNull L listener) {
    synchronized (listeners) {
      listeners.add(listener);
    }
  }

  @Override
  public boolean removeEventListener(L listener) {
    synchronized (listeners) {
      return listeners.remove(listener);
    }
  }

  @Override
  public void dispatchEvent(E event) {
    synchronized (listeners) {
      for (val listener : listeners) {
        dispatchEvent(listener, event);
      }
    }
  }

  protected abstract void dispatchEvent(L listener, E event);
}
