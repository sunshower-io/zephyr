package io.zephyr.kernel;

import io.zephyr.api.ModuleTracker;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.events.Event;
import io.zephyr.kernel.events.EventListener;
import io.zephyr.kernel.events.EventType;
import lombok.AllArgsConstructor;
import lombok.val;

@AllArgsConstructor
@SuppressWarnings("PMD.DoNotUseThreads")
public class AsynchronousModuleThreadTracker implements ModuleTracker {

  final Kernel kernel;
  final TaskQueue taskQueue;

  @Override
  public boolean listensFor(EventType... types) {
    return false;
  }

  @Override
  public <T> void addEventListener(EventListener<T> listener, EventType... types) {

    class DelegatingEventListener<T> implements EventListener<T> {

      @Override
      public void onEvent(EventType type, Event<T> event) {
        taskQueue.schedule(
            new Runnable() {
              @Override
              public void run() {}
            });
      }
    };
    val delegate = new DelegatingEventListener<>();
    kernel.addEventListener(delegate, types);
  }

  @Override
  public <T> void addEventListener(EventListener<T> listener, int options, EventType... types) {}

  @Override
  public <T> void removeEventListener(EventListener<T> listener) {}

  @Override
  public <T> void dispatchEvent(EventType type, Event<T> event) {}
}
