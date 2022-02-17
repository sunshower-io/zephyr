package io.zephyr.kernel.concurrency;

import io.sunshower.gyre.DirectedGraph;
import io.sunshower.gyre.Scope;
import io.zephyr.kernel.log.Logging;
import io.zephyr.kernel.misc.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
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
    switch (process.getMode()) {
      case KernelAllocated:
        workerPool.submitKernelAllocated(result::run);
        break;
      case UserspaceAllocated:
        workerPool.submit(() -> {
          result.run();
          return null;
        });
        break;
      case SingleThreaded:
        Executors.newSingleThreadExecutor().submit(() -> {
          result.run();
          return null;
        });
        break;
    }
    log.log(Level.INFO, "parallel.scheduler.scheduledtask", process);
    return result;
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
      val taskDef = task.getValue();
      try {
        latch.beforeTask(taskDef);
        val result = task.getValue().run(this);
        if (result != null) {
          return result.value;
        }
        return null;
      } catch (TaskException ex) {

        if (ex.getStatus() == TaskStatus.UNRECOVERABLE) {
          task.getValue().setState(Task.State.Failed);
        } else {
          task.getValue().setState(Task.State.Warning);
        }
        latch.onTaskError(taskDef, ex);
        return null;
      } catch (Exception ex) {
        if (log.isLoggable(Level.INFO)) {
          log.log(Level.INFO, "Error processing task " + task.getValue().getName(), ex);
        }
        return null;
      } finally {
        latch.afterTask(taskDef);
        latch.decrement(taskDef);
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

  final class StagedScheduleEnqueuer extends DefaultTaskEventDispatcher<K> implements Runnable {

    final Scope context;
    final Process<K> process;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public StagedScheduleEnqueuer(Process<K> process, Scope context) {
      this.context = context;
      this.process = process;
      attachListeners();
    }

    @Override
    public void run() {
      outer:
      for (val taskSet : process.getTasks()) {

        val latch = new NotifyingLatch<K>(this, taskSet.size());
        val results = new ArrayList<Task>();
        for (val task : taskSet.getTasks()) {
          val ntask = new NotifyingTask<>(task, latch, context);
          workerPool.submit(ntask);
          results.add(task.getValue());
        }
        try {
          latch.await();
          for (val task : results) {
            if (task.getState() == Task.State.Failed) {
              log.log(Level.WARNING, "Task {0} failed--not continuing ", task.getName());
              break outer;
            }
          }

        } catch (InterruptedException e) {
        }
      }
      complete(process);
    }


    void attachListeners() {
      if (process instanceof DefaultProcess<K>) {
        val proc = (DefaultProcess<K>) process;
        for (val listener : proc.getListeners()) {
          addEventListener(listener.fst, listener.snd);
        }
        for (val disposer : proc.getDisposers()) {
          disposer.set(this);
        }
      }
    }
  }
}
