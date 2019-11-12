package io.sunshower.kernel.concurrency;

import static java.lang.String.format;

import io.sunshower.gyre.*;
import java.util.Iterator;
import java.util.List;
import lombok.val;

/** @param <T> */
@SuppressWarnings({"PMD.AvoidFieldNameMatchingMethodName", "PMD.AvoidUsingVolatile"})
public class DefaultProcess<T> implements Process<T> {
  final String name;
  final boolean coalesce;
  final boolean parallel;
  final Scope context;
  final TaskGraph<T> graph;

  private volatile Schedule<DirectedGraph.Edge<T>, Task> schedule;

  public DefaultProcess(
      String name, boolean coalesce, boolean parallel, Scope context, TaskGraph<T> graph) {
    this.name = name;
    this.coalesce = coalesce;
    this.parallel = parallel;
    this.context = context;
    this.graph = graph;
  }

  @Override
  public String toString() {
    return format("Process(name=%s, coalesce paths=%b, parallel=%b)", name, coalesce, parallel);
  }

  @Override
  public boolean coalesce() {
    return coalesce;
  }

  @Override
  public boolean isParallel() {
    return parallel;
  }

  @Override
  public Scope getContext() {
    return context;
  }

  @Override
  public Graph<DirectedGraph.Edge<T>, Task> getExecutionGraph() {
    return graph;
  }

  @Override
  public List<TaskSet<DirectedGraph.Edge<T>, Task>> getTasks() {
    var local = schedule;
    if (local == null) {
      synchronized (this) {
        local = schedule;
        if (local == null) {
          val cycles = new StronglyConnectedComponents<DirectedGraph.Edge<T>, Task>();
          val partition = cycles.apply(graph);
          if (partition.isCyclic()) {
            throw new IllegalStateException("Cycle detected");
          }
          schedule = local = new ParallelScheduler<DirectedGraph.Edge<T>, Task>().apply(graph);
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
  public TaskSet<DirectedGraph.Edge<T>, Task> get(int i) {
    return getTasks().get(i);
  }

  @Override
  public Schedule<DirectedGraph.Edge<T>, Task> reverse() {
    getTasks();
    return schedule.reverse();
  }

  @Override
  public Iterator<TaskSet<DirectedGraph.Edge<T>, Task>> iterator() {
    return getTasks().iterator();
  }
}
