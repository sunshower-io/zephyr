package io.zephyr.kernel.concurrency;

import io.zephyr.api.Tracker;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.TaskQueue;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.events.*;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import lombok.AllArgsConstructor;
import lombok.val;

@SuppressWarnings({"PMD.DoNotUseThreads"})
public abstract class AbstractAsynchronousObjectTracker<T> implements Tracker<T>, EventListener<T> {

  /** immutable state */
  final Module host;

  final Kernel kernel;

  final TaskQueue taskQueue;
  final Predicate<T> filter;
  final EventSource delegatedEventSource;

  private final List<EventSetTracker> eventSets;
  private final Runnable existingObjectDispatchTask;
  private final List<ObjectEventDispatchState<T>> tracked;

  public AbstractAsynchronousObjectTracker(
      Kernel kernel, Module host, TaskQueue taskQueue, Predicate<T> filter) {
    this.host = host;
    this.kernel = kernel;
    this.filter = filter;
    this.taskQueue = taskQueue;
    this.tracked = new ArrayList<>(0);
    this.eventSets = new ArrayList<>(0);
    this.delegatedEventSource = new ObjectThreadEventSource();
    this.existingObjectDispatchTask = createExistingObjectDispatcher();
  }

  protected abstract Runnable createExistingObjectDispatcher();

  @Override
  public void onEvent(EventType type, Event<T> event) {
    taskQueue.schedule(new FilteredObjectDispatchTask(type, event));
  }

  @Override
  public int getListenerCount() {
    return delegatedEventSource.getListenerCount();
  }

  @Override
  public boolean listensFor(EventType... types) {
    return delegatedEventSource.listensFor(types);
  }

  @AllArgsConstructor
  final class EventSetTracker {
    final int options;
    final EventType[] types;
    final EventListener<?> listener;

    void start() {
      delegatedEventSource.addEventListener(listener, options, types);
      kernel.addEventListener(AbstractAsynchronousObjectTracker.this, types);
    }

    void stop() {
      delegatedEventSource.removeEventListener(listener);
      removeEventListener(listener);
      kernel.removeEventListener(AbstractAsynchronousObjectTracker.this);
    }
  }

  @Override
  public <U> void addEventListener(EventListener<U> listener, EventType... types) {
    addEventListener(listener, Options.NONE, types);
  }

  @Override
  public <U> void addEventListener(EventListener<U> listener, int options, EventType... types) {
    val tracker = new EventSetTracker(options, types, listener);
    eventSets.add(tracker);
    tracker.start();
    fireExistingModuleEvents();
  }

  private void fireExistingModuleEvents() {
    taskQueue.schedule(existingObjectDispatchTask);
  }

  @Override
  public <U> void removeEventListener(EventListener<U> listener) {
    val iter = eventSets.iterator();
    while (iter.hasNext()) {
      val next = iter.next();
      if (next.listener == listener) {
        iter.remove();
      }
    }
  }

  @Override
  public <U> void dispatchEvent(EventType type, Event<U> event) {
    delegatedEventSource.dispatchEvent(type, event);
  }

  @Override
  public void close() {

    synchronized (eventSets) {
      stop();
      eventSets.clear();
    }
  }

  @Override
  public void stop() {
    synchronized (eventSets) {
      val iter = eventSets.stream().iterator();
      while (iter.hasNext()) {
        iter.next().stop();
      }
    }
  }

  @Override
  public void start() {
    synchronized (eventSets) {
      val iter = eventSets.stream().iterator();
      while (iter.hasNext()) {
        iter.next().start();
      }
    }
  }

  @Override
  public List<T> getTracked() {
    synchronized (tracked) {
      val results = new ArrayList<T>(tracked.size());
      for (val module : tracked) {
        results.add(module.object);
      }
      return results;
    }
  }

  @Override
  public int getTrackedCount() {
    return tracked.size();
  }

  @Override
  public void waitUntil(Predicate<? super Collection<T>> condition) {
    for (; ; ) {
      synchronized (tracked) {
        if (condition.test(getTracked())) {
          return;
        }
        try {
          tracked.wait();
        } catch (InterruptedException ex) {
        }
      }
    }
  }

  @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
  private void track(EventType events, T object) {
    synchronized (tracked) {
      boolean found = false;
      for (val trackedModule : tracked) {
        if (trackedModule.object == object) {
          trackedModule.set(events);
          found = true;
          break;
        }
      }
      if (!found) {
        val trackedModule = new ObjectEventDispatchState<T>(object);
        trackedModule.set(events);
        tracked.add(trackedModule);
      }
      tracked.notifyAll();
    }
  }

  final class FilteredObjectDispatchTask implements Runnable {

    final EventType type;
    final Event<T> event;

    FilteredObjectDispatchTask(EventType type, Event<T> event) {
      this.type = type;
      this.event = event;
    }

    @Override
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    public void run() {
      val target = event.getTarget();
      if (!(target == host || isTracked(type, target))) {
        if (filter.test(target)) {
          track(type, target);
          dispatchEvent(type, event);
        }
      }
    }
  }

  private boolean isTracked(EventType type, T target) {
    synchronized (tracked) {
      for (val trackedObject : tracked) {

        if (target == trackedObject.object && trackedObject.hasFired(type)) {
          return true;
        }
      }
      return false;
    }
  }

  static final class ObjectThreadEventSource extends AbstractEventSource {}

  static final class ObjectEventDispatchState<T> {
    final T object;
    final BitSet events;

    ObjectEventDispatchState(final T object) {
      this.object = object;
      this.events = new BitSet();
    }

    void set(EventType events) {
      this.events.set(events.getId());
    }

    void clear(EventType events) {
      this.events.clear(events.getId());
    }

    boolean hasFired(EventType events) {
      return this.events.get(events.getId());
    }
  }
}
