package io.zephyr.kernel.concurrency;

import io.zephyr.api.ServiceReference;
import io.zephyr.api.ServiceTracker;
import io.zephyr.kernel.events.Event;
import io.zephyr.kernel.events.EventListener;
import io.zephyr.kernel.events.EventType;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class AsynchronousServiceTracker implements ServiceTracker {
  @Override
  public void close() {}

  @Override
  public void stop() {

  }

  @Override
  public List<ServiceReference<?>> getTracked() {
    return null;
  }

  @Override
  public int getTrackedCount() {
    return 0;
  }

  @Override
  public void waitUntil(Predicate<? super Collection<ServiceReference<?>>> condition) {}

  @Override
  public int getListenerCount() {
    return 0;
  }

  @Override
  public boolean listensFor(EventType... types) {
    return false;
  }

  @Override
  public <T> void addEventListener(EventListener<T> listener, EventType... types) {}

  @Override
  public <T> void addEventListener(EventListener<T> listener, int options, EventType... types) {}

  @Override
  public <T> void removeEventListener(EventListener<T> listener) {}

  @Override
  public <T> void dispatchEvent(EventType type, Event<T> event) {}

  @Override
  public void start() {

  }
}
