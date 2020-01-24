package io.zephyr.kernel;

import io.zephyr.api.ModuleTracker;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.events.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

@SuppressWarnings("PMD.DoNotUseThreads")
public class AsynchronousModuleThreadTracker implements ModuleTracker, EventListener<Module> {

  /** immutable state */
  final Kernel kernel;

  final ModuleThread taskQueue;
  final Predicate<Module> filter;
  final EventSource delegatedEventSource;

  /** mutable state */
  private final List<Module> tracked;

  public AsynchronousModuleThreadTracker(
      Kernel kernel, ModuleThread taskQueue, Predicate<Module> filter) {
    this.kernel = kernel;
    this.filter = filter;
    this.taskQueue = taskQueue;
    this.delegatedEventSource = new ModuleThreadEventSource();
    this.tracked = new ArrayList<>(0);
  }

  @Override
  public void onEvent(EventType type, Event<Module> event) {
    taskQueue.schedule(new FilteredModuleDispatchTask(type, event));
  }

  @Override
  public boolean listensFor(EventType... types) {
    return delegatedEventSource.listensFor(types);
  }

  @Override
  public <T> void addEventListener(EventListener<T> listener, EventType... types) {
    delegatedEventSource.addEventListener(listener, types);
    kernel.addEventListener(this, types);
  }

  @Override
  public <T> void addEventListener(EventListener<T> listener, int options, EventType... types) {
    delegatedEventSource.addEventListener(listener, options, types);
    kernel.addEventListener(this, options, types);
  }

  @Override
  public <T> void removeEventListener(EventListener<T> listener) {
    delegatedEventSource.removeEventListener(listener);
  }

  @Override
  public <T> void dispatchEvent(EventType type, Event<T> event) {
    delegatedEventSource.dispatchEvent(type, event);
  }

  @Override
  public void close() throws IOException {
    kernel.removeEventListener(this);
  }

  @Override
  public List<Module> getTracked() {
    return Collections.unmodifiableList(tracked);
  }

  @Override
  public int getTrackedCount() {
    return tracked.size();
  }

  @Override
  public void waitUntil(Predicate<List<Module>> condition) {
    for (; ; ) {
      synchronized (tracked) {
        if (condition.test(tracked)) {
          return;
        }
        try {
          tracked.wait();
        } catch (InterruptedException ex) {
        }
      }
    }
  }

  private void track(Module module) {
    synchronized (tracked) {
      tracked.add(module);
      tracked.notifyAll();
    }
  }

  final class FilteredModuleDispatchTask implements Runnable {

    final EventType type;
    final Event<Module> event;

    FilteredModuleDispatchTask(EventType type, Event<Module> event) {
      this.type = type;
      this.event = event;
    }

    @Override
    public void run() {
      if (filter.test(event.getTarget())) {
        track(event.getTarget());
        dispatchEvent(type, event);
      }
    }
  }

  static final class ModuleThreadEventSource extends AbstractEventSource {}
}
