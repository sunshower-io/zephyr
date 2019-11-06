package io.sunshower.kernel.concurrency;

import io.sunshower.gyre.DirectedGraph;
import java.util.concurrent.Callable;
import lombok.val;

@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
public class TopologyAwareParallelScheduler<K> {
  private final WorkerPool workerPool;

  public TopologyAwareParallelScheduler(WorkerPool workerPool) {
    this.workerPool = workerPool;
  }

  /**
   * this method does not block
   *
   * @param process process to submit
   * @param context
   * @return a task listener for the given process
   */
  public TaskTracker<K> submit(Process<K> process, Context context) {
    val result = new StagedScheduleEnqueuer(process, context);
    workerPool.submitKernelAllocated(result);
    return result;
  }

  final class StagedScheduleEnqueuer extends DefaultTaskEventDispatcher<K> implements Runnable {

    final Context context;
    final Process<K> process;

    public StagedScheduleEnqueuer(Process<K> process, Context context) {
      this.context = context;
      this.process = process;
    }

    @Override
    public void run() {
      for (val taskSet : process.getTasks()) {
        val latch = new NotifyingLatch<K>(this, taskSet.size());
        for (val task : taskSet.getTasks()) {
          workerPool.submit(new NotifyingTask<>(task, latch, context));
        }
        try {
          latch.await();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      complete(null);
    }
  }

  private static class NotifyingTask<K, V> implements Callable<V> {
    private final Context context;
    private final NotifyingLatch<K> latch;
    private final io.sunshower.gyre.Task<DirectedGraph.Edge<K>, Task> task;

    public NotifyingTask(
        io.sunshower.gyre.Task<DirectedGraph.Edge<K>, Task> task,
        NotifyingLatch<K> latch,
        Context context) {
      this.task = task;
      this.latch = latch;
      this.context = context;
    }

    @Override
    @SuppressWarnings("unchecked")
    public V call() throws Exception {
      latch.beforeTask();
      val result = task.getValue().run(context);
      latch.decrement();
      latch.afterTask();
      return (V) result.value;
    }
  }
}
