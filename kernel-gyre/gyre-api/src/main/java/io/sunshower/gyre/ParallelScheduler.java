package io.sunshower.gyre;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.val;

import java.util.*;
import java.util.function.Predicate;

public class ParallelScheduler<E, V> implements Transformation<E, V, Schedule<E, V>> {

  @Override
  public Schedule<E, V> apply(Graph<E, V> graph) {
    return apply(graph, EdgeFilters.acceptAll(), NodeFilters.acceptAll());
  }

  @Override
  public Schedule<E, V> apply(Graph<E, V> graph, Predicate<E> edgeFilter, Predicate<V> nodeFilter) {
    val copy = graph.clone();
    val result = new MutableSchedule<E, V>();
    while (!copy.isEmpty()) {
      result.tasks.add(collectAndRemove(copy, edgeFilter));
    }
    return result;
  }

  private TaskSet<E, V> collectAndRemove(Graph<E, V> copy, Predicate<E> edgeFilter) {
    val taskSet = new MutableTaskSet<E, V>();
    for (val v : copy.vertexSet()) {
      if (copy.degreeOf(v, edgeFilter) == 0) {
        val edges = copy.remove(v, edgeFilter);
        taskSet.tasks.add(new LabeledTask<>(v, edges));
      }
    }
    return taskSet;
  }

  static final class MutableSchedule<E, V> implements Schedule<E, V> {

    final List<TaskSet<E, V>> tasks;

    MutableSchedule() {
      tasks = new ArrayList<>();
    }

    @Override
    public List<TaskSet<E, V>> getTasks() {
      return Collections.unmodifiableList(tasks);
    }

    @Override
    public int size() {
      return tasks.size();
    }

    @Override
    public TaskSet<E, V> get(int i) {
      return tasks.get(i);
    }

    @Override
    public Iterator<TaskSet<E, V>> iterator() {
      return tasks.iterator();
    }
  }

  @AllArgsConstructor
  static final class LabeledTask<E, V> implements Task<E, V> {
    final V value;
    final Set<E> edges;

    @Override
    public V getValue() {
      return value;
    }

    @Override
    public Set<E> getEdges() {
      return edges;
    }
  }

  static final class MutableTaskSet<E, V> implements TaskSet<E, V> {
    final List<Task<E, V>> tasks;

    MutableTaskSet(List<Task<E, V>> tasks) {
      this.tasks = tasks;
    }

    MutableTaskSet(Task<E, V> task) {
      this(Collections.singletonList(task));
    }

    MutableTaskSet() {
      tasks = new ArrayList<>();
    }

    @Override
    public List<Task<E, V>> getTasks() {
      return tasks;
    }

    @Override
    public int size() {
      return tasks.size();
    }
  }
}
