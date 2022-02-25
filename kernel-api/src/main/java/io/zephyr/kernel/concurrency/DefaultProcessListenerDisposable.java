package io.zephyr.kernel.concurrency;

import io.sunshower.gyre.Pair;
import io.zephyr.api.Disposable;
import io.zephyr.kernel.events.EventListener;
import io.zephyr.kernel.events.EventSource;
import java.util.List;

class DefaultProcessListenerDisposable implements Disposable {

  private final Pair<TaskEventType, EventListener<Task>> listener;
  private final List<DefaultProcessListenerDisposable> disposers;
  private final List<Pair<TaskEventType, EventListener<Task>>> listeners;
  private EventSource source;

  public DefaultProcessListenerDisposable(
      Pair<TaskEventType, EventListener<Task>> result,
      List<Pair<TaskEventType, EventListener<Task>>> listeners,
      List<DefaultProcessListenerDisposable> disposers) {
    this.listener = result;
    this.disposers = disposers;
    this.listeners = listeners;
  }

  @Override
  public void dispose() {
    if (source != null) {
      source.removeEventListener(listener.snd);
    }
    listeners.remove(listener);
    disposers.remove(this);
  }

  public <K> void set(EventSource source) {
    this.source = source;
  }
}
