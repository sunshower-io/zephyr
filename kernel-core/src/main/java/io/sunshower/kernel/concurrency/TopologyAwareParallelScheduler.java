package io.sunshower.kernel.concurrency;

import io.sunshower.gyre.DirectedGraph;
import io.sunshower.kernel.log.Logger;
import io.sunshower.kernel.log.Logging;
import io.sunshower.kernel.misc.SuppressFBWarnings;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import lombok.val;

@SuppressFBWarnings
@SuppressWarnings({
  "PMD.AvoidInstantiatingObjectsInLoops",
  "PMD.DoNotUseThreads",
  "PMD.AvoidUsingVolatile"
})
public class TopologyAwareParallelScheduler<K> {
  static final Logger log = Logging.get(TopologyAwareParallelScheduler.class, "Concurrency");
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
    log.log(Level.INFO, "parallel.scheduler.schedulingtask", process);
    val result = new StagedScheduleEnqueuer(process, context);
    workerPool.submitKernelAllocated(result);
    return result;
  }

  final class StagedScheduleEnqueuer extends DefaultTaskEventDispatcher<K> implements Runnable {

    final Context context;
    final Process<K> process;
    final ReductionScope rootScope;
    volatile ReductionScope currentScope;

    public StagedScheduleEnqueuer(Process<K> process, Context context) {
      this.context = context;
      this.process = process;
      rootScope = ReductionScope.newRoot(context);
      currentScope = rootScope;
    }

    @Override
    public void run() {
      for (val taskSet : process.getTasks()) {

        val latch = new NotifyingLatch<K>(this, taskSet.size());
        currentScope = currentScope.pushScope(taskSet);
        for (val task : taskSet.getTasks()) {
          currentScope = currentScope.pushScope(task);
          workerPool.submit(new NotifyingTask<>(task, latch, currentScope));
        }
        try {
          latch.await();
        } catch (InterruptedException e) {
          // eh

        }
      }
      complete(null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Context getRootScope() {
      return rootScope;
    }

    @Override
    public Context getCurrentScope() {
      return currentScope;
    }
  }

  private static class NotifyingTask<K> implements Callable<Object> {
    private final ReductionScope scope;
    private final NotifyingLatch<K> latch;
    private final io.sunshower.gyre.Task<DirectedGraph.Edge<K>, Task> task;

    public NotifyingTask(
        io.sunshower.gyre.Task<DirectedGraph.Edge<K>, Task> task,
        NotifyingLatch<K> latch,
        final ReductionScope scope) {
      this.task = task;
      this.latch = latch;
      this.scope = scope;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object call() throws Exception {
      try {
        latch.beforeTask();
        val result = task.getValue().run(scope);
        return result.value;
      } finally {
        latch.decrement();
        latch.afterTask();
        scope.popScope();
      }
    }
  }
}
