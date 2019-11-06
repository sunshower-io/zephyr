package io.sunshower.kernel.concurrency;

public interface TaskEventDispatcher<K> {
  void dispatch(TaskEventType type, TaskPhaseEvent taskPhaseEvent);
}
