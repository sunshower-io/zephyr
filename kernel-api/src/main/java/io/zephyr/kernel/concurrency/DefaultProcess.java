package io.zephyr.kernel.concurrency;

import static java.lang.String.format;

import io.sunshower.gyre.DirectedGraph;
import io.sunshower.gyre.Graph;
import io.sunshower.gyre.Pair;
import io.sunshower.gyre.ParallelScheduler;
import io.sunshower.gyre.Schedule;
import io.sunshower.gyre.Scope;
import io.sunshower.gyre.StronglyConnectedComponents;
import io.sunshower.gyre.TaskSet;
import io.sunshower.lang.events.EventListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;

/** @param <T> */
@SuppressWarnings({"PMD.AvoidFieldNameMatchingMethodName", "PMD.AvoidUsingVolatile"})
public class DefaultProcess<T> implements Process<T> {

  final String name;
  final boolean coalesce;
  final Scope context;
  @Getter final DirectedGraph<T, Task> graph;
  @Getter private final List<DefaultProcessListenerDisposable> disposers;
  @Getter private final List<Pair<TaskEventType, EventListener<Task>>> listeners;
  private Mode mode;
  private volatile Schedule<DirectedGraph.Edge<T>, io.zephyr.kernel.concurrency.Task> schedule;

  public DefaultProcess(
      String name,
      boolean coalesce,
      boolean parallel,
      Scope context,
      DirectedGraph<T, Task> graph) {
    this.name = name;
    this.coalesce = coalesce;
    this.context = context;
    this.graph = graph;
    if (!parallel) {
      setMode(Mode.SingleThreaded);
    } else {
      setMode(Mode.KernelAllocated);
    }
    this.listeners = new ArrayList<>();
    this.disposers = new ArrayList<>();
  }

  @Override
  public String toString() {
    return format("Process(name=%s, coalesce paths=%b, parallel=%b)", name, coalesce, isParallel());
  }

  @Override
  public Mode getMode() {
    return mode;
  }

  @Override
  public void setMode(@NonNull Mode mode) {
    this.mode = mode;
  }

  @Override
  public boolean coalesce() {
    return coalesce;
  }

  @Override
  public boolean isParallel() {
    return mode != Mode.SingleThreaded;
  }

  @Override
  public Scope getContext() {
    return context;
  }

  @Override
  public Graph<DirectedGraph.Edge<T>, io.zephyr.kernel.concurrency.Task> getExecutionGraph() {
    return graph;
  }

  @Override
  public DefaultProcessListenerDisposable addEventListener(
      TaskEventType type, EventListener<Task> listener) {
    val result = Pair.of(type, listener);
    listeners.add(result);
    val disposable = new DefaultProcessListenerDisposable(result, listeners, disposers);
    disposers.add(disposable);
    return disposable;
  }

  @Override
  public List<TaskSet<DirectedGraph.Edge<T>, io.zephyr.kernel.concurrency.Task>> getTasks() {
    var local = schedule;
    if (local == null) {
      synchronized (this) {
        local = schedule;
        if (local == null) {
          val cycles =
              new StronglyConnectedComponents<
                  DirectedGraph.Edge<T>, io.zephyr.kernel.concurrency.Task>();
          val partition = cycles.apply(graph);
          if (partition.isCyclic()) {
            throw new IllegalStateException("Cycle detected: " + partition.getElements());
          }
          schedule =
              local =
                  new ParallelScheduler<DirectedGraph.Edge<T>, io.zephyr.kernel.concurrency.Task>()
                      .apply(graph);
        }
      }
    }
    return local.getTasks();
  }

  @Override
  public int size() {
    return getTasks().size();
  }

  @Override
  public TaskSet<DirectedGraph.Edge<T>, io.zephyr.kernel.concurrency.Task> get(int i) {
    return getTasks().get(i);
  }

  @Override
  public Schedule<DirectedGraph.Edge<T>, io.zephyr.kernel.concurrency.Task> reverse() {
    getTasks();
    return schedule.reverse();
  }

  @Override
  public Iterator<TaskSet<DirectedGraph.Edge<T>, Task>> iterator() {
    return getTasks().iterator();
  }
}
