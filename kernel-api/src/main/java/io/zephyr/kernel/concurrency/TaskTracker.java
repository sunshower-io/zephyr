package io.zephyr.kernel.concurrency;

import io.zephyr.api.Disposable;
import io.zephyr.kernel.events.EventListener;
import io.zephyr.kernel.events.EventSource;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;

public interface TaskTracker<E>
    extends CompletionStage<Process<E>>, Future<Process<E>>, EventSource {


  Disposable addEventListener(TaskEventType type, EventListener<Task> listener);

}
