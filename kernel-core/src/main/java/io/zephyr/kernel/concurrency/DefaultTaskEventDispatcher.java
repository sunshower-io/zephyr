package io.zephyr.kernel.concurrency;

import io.sunshower.lang.events.AbstractEventSource;
import io.sunshower.lang.events.EventListener;
import io.sunshower.lang.events.EventSource;
import io.zephyr.api.Disposable;
import io.zephyr.kernel.events.KernelEvents;
import io.zephyr.kernel.status.Status;
import java.util.concurrent.CompletableFuture;
import lombok.experimental.Delegate;
import lombok.val;

class DefaultTaskEventDispatcher<K> extends CompletableFuture<Process<K>>
    implements TaskTracker<K>, TaskEventDispatcher<K> {

  @Delegate private final EventSource delegate;

  DefaultTaskEventDispatcher() {
    delegate = new TaskTrackerEventSource();
  }

  @Override
  public void dispatch(TaskEventType type, TaskPhaseEvent taskPhaseEvent) {
    val event =
        KernelEvents.create(taskPhaseEvent.getTask(), new Status(type.getStatusType(), "", false));
    delegate.dispatchEvent(type, event);
  }

  @Override
  public Disposable addEventListener(TaskEventType type, EventListener<Task> listener) {
    delegate.addEventListener(listener, type);
    return () -> delegate.removeEventListener(listener);
  }

  static final class TaskTrackerEventSource extends AbstractEventSource {}
}
