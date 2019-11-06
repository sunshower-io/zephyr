package io.sunshower.kernel.concurrency;

import java.util.concurrent.CompletableFuture;

class DefaultTaskEventDispatcher<K> extends CompletableFuture<Context>
    implements TaskTracker<K>, TaskEventDispatcher<K> {

  @Override
  public void dispatch(TaskEventType type, TaskPhaseEvent taskPhaseEvent) {}
}
