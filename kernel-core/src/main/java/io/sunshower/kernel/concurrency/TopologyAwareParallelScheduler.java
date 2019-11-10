package io.sunshower.kernel.concurrency;

import io.sunshower.gyre.DirectedGraph;
import io.sunshower.gyre.Scope;
import io.sunshower.kernel.log.Logging;
import io.sunshower.kernel.misc.SuppressFBWarnings;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
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
  public TaskTracker<K> submit(Process<K> process, Scope context) {
    log.log(Level.INFO, "parallel.scheduler.schedulingtask", process);
    val result = new StagedScheduleEnqueuer(process, context);
    workerPool.submitKernelAllocated(result);
    log.log(Level.INFO, "parallel.scheduler.scheduledtask", process);
    return result;
  }

  final class StagedScheduleEnqueuer extends DefaultTaskEventDispatcher<K> implements Runnable {

    final Scope context;
    final Process<K> process;
    final Object lock = new Object();

    public StagedScheduleEnqueuer(Process<K> process, Scope context) {
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
        }
      }
      complete(null);
    }
  }

  private static class NotifyingTask<K> implements Callable<Object>, Scope {
    private final Scope scope;
    private final NotifyingLatch<K> latch;
    private final io.sunshower.gyre.Task<DirectedGraph.Edge<K>, Task> task;

    public NotifyingTask(
        io.sunshower.gyre.Task<DirectedGraph.Edge<K>, Task> task,
        NotifyingLatch<K> latch,
        final Scope scope) {
      this.task = task;
      this.latch = latch;
      this.scope = scope;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object call() throws Exception {
      try {
        latch.beforeTask();
        val result = task.getValue().run(this);
        if (result != null) {
          return result.value;
        }
        return null;
      } catch (Exception ex) {
        if (log.isLoggable(Level.INFO)) {
          log.log(Level.INFO, "Error processing task " + task.getValue().getName(), ex);
        }
        return null;
      } finally {
        latch.decrement();
        latch.afterTask();
      }
    }

    @Override
    public <T> void set(String name, T value) {
      val tscope = task.getScope();
      tscope.set(name, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String name) {
      val tscope = task.getScope();
      val result = tscope.get(name);
      if (result == null) {
        return scope.get(name);
      }
      return (T) result;
    }

    @Override
    public <E> E computeIfAbsent(String scannedPlugins, E o) {
      return task.getScope().computeIfAbsent(scannedPlugins, o);
    }
  }
}
