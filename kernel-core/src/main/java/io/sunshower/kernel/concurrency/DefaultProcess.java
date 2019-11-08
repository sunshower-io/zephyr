package io.sunshower.kernel.concurrency;

import static java.lang.String.format;

import io.sunshower.gyre.DirectedGraph;
import io.sunshower.gyre.Graph;
import io.sunshower.gyre.ParallelScheduler;
import io.sunshower.gyre.TaskSet;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public class DefaultProcess<T> implements Process<T> {
  final String name;
  final boolean coalesce;
  final boolean parallel;
  final Context context;
  final TaskGraph<T> graph;

  public DefaultProcess(
      String name, boolean coalesce, boolean parallel, Context context, TaskGraph<T> graph) {
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
    return false;
  }

  @Override
  public boolean isParallel() {
    return false;
  }

  @Override
  public Context getContext() {
    return context;
  }

  @Override
  public Graph<DirectedGraph.Edge<T>, Task> getExecutionGraph() {
    return graph;
  }

  @Override
  public List<TaskSet<DirectedGraph.Edge<T>, Task>> getTasks() {
    return new ParallelScheduler<DirectedGraph.Edge<T>, Task>().apply(graph).getTasks();
  }

  @Override
  public int size() {
    return 0;
  }

  @Override
  public TaskSet<DirectedGraph.Edge<T>, Task> get(int i) {
    return null;
  }

  @Override
  public Iterator<TaskSet<DirectedGraph.Edge<T>, Task>> iterator() {
    return null;
  }
}
