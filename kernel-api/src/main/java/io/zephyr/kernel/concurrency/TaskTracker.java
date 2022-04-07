package io.zephyr.kernel.concurrency;

import io.sunshower.lang.events.EventListener;
import io.sunshower.lang.events.EventSource;
import io.zephyr.api.Disposable;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;

public interface TaskTracker<E>
    extends CompletionStage<Process<E>>, Future<Process<E>>, EventSource {

  Disposable addEventListener(TaskEventType type, EventListener<Task> listener);
}
