package io.zephyr.kernel.concurrency;

import java.util.concurrent.CountDownLatch;

public class NotifyingLatch<K> {

  final CountDownLatch latch;
  final TaskEventDispatcher<K> dispatcher;

  public NotifyingLatch(TaskEventDispatcher<K> dispatcher, int size) {
    this.dispatcher = dispatcher;
    this.latch = new CountDownLatch(size);
  }

  void start(Task task) {
    dispatcher.dispatch(TaskEvents.TASK_PHASE_STARTING, new TaskPhaseEvent(task));
  }

  void beforeTask(Task task) {
    dispatcher.dispatch(TaskEvents.TASK_STARTING, new TaskPhaseEvent(task));
  }

  void afterTask(Task task) {
    dispatcher.dispatch(TaskEvents.TASK_STARTING, new TaskPhaseEvent(task));
  }

  /**
   * should be called between beforeTask() and afterTask()
   */
  void decrement(Task task) {
    latch.countDown();
    dispatcher.dispatch(TaskEvents.TASK_COMPLETE, new TaskPhaseEvent(task));
  }

  void await() throws InterruptedException {
    try {
      latch.await();
    } catch (InterruptedException ex) {

    } finally {
      dispatcher.dispatch(TaskEvents.TASK_PHASE_COMPLETE, new TaskPhaseEvent(null));
    }
  }

  public void onTaskError(Task taskDef, TaskException ex) {
    dispatcher.dispatch(TaskEvents.TASK_ERROR, new TaskPhaseEvent(taskDef, ex));

  }
}
