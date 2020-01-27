package io.zephyr.kernel.concurrency;

import io.zephyr.api.ModuleEvents;
import io.zephyr.api.ModuleTracker;
import io.zephyr.kernel.Lifecycle;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.events.*;
import io.zephyr.kernel.events.EventListener;
import java.util.*;
import java.util.function.Predicate;
import lombok.val;

@SuppressWarnings({
  "PMD.DataflowAnomalyAnalysis",
  "PMD.CompareObjectsWithEquals",
  "PMD.DoNotUseThreads",
  "PMD.AvoidInstantiatingObjectsInLoops",
  "PMD.UnusedPrivateMethod"
})
public class AsynchronousModuleThreadTracker implements ModuleTracker, EventListener<Module> {

  /** immutable state */
  final Module host;

  final Kernel kernel;

  final ModuleThread taskQueue;
  final Predicate<Module> filter;
  final EventSource delegatedEventSource;

  final Runnable existingModuleDispatchTask;
  private final List<ModuleEventDispatchState> tracked;

  public AsynchronousModuleThreadTracker(
      Kernel kernel, Module host, ModuleThread taskQueue, Predicate<Module> filter) {
    this.host = host;
    this.kernel = kernel;
    this.filter = filter;
    this.taskQueue = taskQueue;
    this.delegatedEventSource = new ModuleThreadEventSource();
    this.tracked = new ArrayList<>(0);
    existingModuleDispatchTask = new ExistingModuleDispatchTask();
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
    fireExistingModuleEvents();
  }

  @Override
  public <T> void addEventListener(EventListener<T> listener, int options, EventType... types) {
    delegatedEventSource.addEventListener(listener, options, types);
    kernel.addEventListener(this, options, types);
    fireExistingModuleEvents();
  }

  private void fireExistingModuleEvents() {
    taskQueue.schedule(existingModuleDispatchTask);
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
  public void close() {
    kernel.removeEventListener(this);
  }

  @Override
  public List<Module> getTracked() {
    synchronized (tracked) {
      val results = new ArrayList<Module>(tracked.size());
      for (val module : tracked) {
        results.add(module.module);
      }
      return results;
    }
  }

  @Override
  public int getTrackedCount() {
    return tracked.size();
  }

  @Override
  public void waitUntil(Predicate<? super Collection<Module>> condition) {
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

  private void track(EventType events, Module module) {
    synchronized (tracked) {
      boolean found = false;
      for (val trackedModule : tracked) {
        if (trackedModule.module == module) {
          trackedModule.set(events);
          found = true;
          break;
        }
      }
      if (!found) {
        val trackedModule = new ModuleEventDispatchState(module);
        trackedModule.set(events);
        tracked.add(trackedModule);
      }
      tracked.notifyAll();
    }
  }

  final class ExistingModuleDispatchTask implements Runnable {

    @Override
    public void run() {
      val modules = kernel.getModuleManager().getModules();
      for (val module : modules) {
        val lifecycle = module.getLifecycle();
        val state = lifecycle.getState();
        if (state == Lifecycle.State.Installed) {
          taskQueue.schedule(
              new FilteredModuleDispatchTask(ModuleEvents.INSTALLED, Events.create(module)));
        } else if (state == Lifecycle.State.Active) {
          taskQueue.schedule(
              new FilteredModuleDispatchTask(ModuleEvents.INSTALLED, Events.create(module)));
          taskQueue.schedule(
              new FilteredModuleDispatchTask(ModuleEvents.STARTED, Events.create(module)));
        }
      }
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
      val target = event.getTarget();
      if (!(target == host || isTracked(type, target))) {
        if (filter.test(target)) {
          track(type, target);
          dispatchEvent(type, event);
        }
      }
    }
  }

  private boolean isTracked(EventType type, Module target) {
    synchronized (tracked) {
      for (val trackedModule : tracked) {

        if (target == trackedModule.module && trackedModule.hasFired(type)) {
          return true;
        }
      }
      return false;
    }
  }

  static final class ModuleThreadEventSource extends AbstractEventSource {}

  static final class ModuleEventDispatchState {
    final Module module;
    final BitSet events;

    ModuleEventDispatchState(final Module module) {
      this.module = module;
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