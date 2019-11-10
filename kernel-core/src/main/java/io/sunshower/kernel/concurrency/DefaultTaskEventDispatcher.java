package io.sunshower.kernel.concurrency;

import java.util.concurrent.CompletableFuture;

class DefaultTaskEventDispatcher<K> extends CompletableFuture<Process<K>>
    implements TaskTracker<K>, TaskEventDispatcher<K> {

  @Override
  public void dispatch(TaskEventType type, TaskPhaseEvent taskPhaseEvent) {}
}
