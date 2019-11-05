package io.sunshower.gyre;

import lombok.AllArgsConstructor;
import lombok.val;

import java.util.*;
import java.util.function.Predicate;

public class ParallelScheduler<E, V> implements Transformation<E, V, ParallelSchedule<E, V>> {

  @Override
  public ParallelSchedule<E, V> apply(Graph<E, V> graph) {
    return apply(graph, EdgeFilters.acceptAll(), NodeFilters.acceptAll());
  }

  @Override
  public ParallelSchedule<E, V> apply(
      Graph<E, V> graph, Predicate<E> edgeFilter, Predicate<V> nodeFilter) {
    val copy = graph.clone();
    val result = new MutableParallelSchedule<E, V>();
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

  static final class MutableParallelSchedule<E, V> implements ParallelSchedule<E, V> {

    final List<TaskSet<E, V>> tasks;

    MutableParallelSchedule() {
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
      return null;
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

    MutableTaskSet() {
      tasks = new ArrayList<>();
    }

    @Override
    public List<Task<E, V>> getTasks() {
      return tasks;
    }
  }
}
