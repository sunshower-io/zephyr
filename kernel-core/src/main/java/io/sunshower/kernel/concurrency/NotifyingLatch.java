package io.sunshower.kernel.concurrency;

import java.util.concurrent.CountDownLatch;

public class NotifyingLatch<K> {
  final CountDownLatch latch;
  final TaskEventDispatcher<K> dispatcher;

  public NotifyingLatch(TaskEventDispatcher<K> dispatcher, int size) {
    this.dispatcher = dispatcher;
    this.latch = new CountDownLatch(size);
  }

  void start() {
    dispatcher.dispatch(TaskEvents.TASK_PHASE_STARTING, new TaskPhaseEvent());
  }

  void beforeTask() {
    dispatcher.dispatch(TaskEvents.TASK_STARTING, new TaskPhaseEvent());
  }

  void afterTask() {
    dispatcher.dispatch(TaskEvents.TASK_STARTING, new TaskPhaseEvent());
  }

  /** should be called between beforeTask() and afterTask() */
  void decrement() {
    latch.countDown();
    dispatcher.dispatch(TaskEvents.TASK_COMPLETE, new TaskPhaseEvent());
  }

  void await() throws InterruptedException {
    try {
      latch.await();
    } catch (InterruptedException ex) {

    } finally {
      dispatcher.dispatch(TaskEvents.TASK_PHASE_COMPLETE, new TaskPhaseEvent());
    }
  }
}
