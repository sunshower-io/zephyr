package io.sunshower.gyre;

import java.util.*;
import java.util.function.Predicate;
import lombok.val;

public class ParallelScheduler<E, V> implements Transformation<E, V, Schedule<E, V>> {

  @Override
  public Schedule<E, V> apply(Graph<E, V> graph) {
    return apply(graph, EdgeFilters.acceptAll(), NodeFilters.acceptAll());
  }

  @Override
  public Schedule<E, V> apply(Graph<E, V> graph, Predicate<E> edgeFilter, Predicate<V> nodeFilter) {
    val copy = removeSelfDependencies(graph);
    val result = new MutableSchedule<E, V>();

    val tasks = new HashMap<V, Task<E, V>>();
    while (!copy.isEmpty()) {
      val frontier = collectFrontier(copy, edgeFilter);

      if (frontier.isEmpty() && !copy.isEmpty()) {
        throw new IllegalStateException("Error: cyclic graph");
      }
      val ts = new MutableTaskSet<E, V>();
      result.tasks.add(ts);
      for (val node : frontier) {
        copy.delete(node);
        val task = new LabeledTask<E, V>(node, Collections.emptySet(), new HashSet<>());
        tasks.put(node, task);
        ts.tasks.add(task);
      }

      for (val node : frontier) {
        LabeledTask<E, V> actualTask = (LabeledTask<E, V>) tasks.get(node);

        if (actualTask == null) {
          throw new IllegalStateException("weird--coulda sworn that task was right there");
        }

        for (val neighbor : graph.neighbors(node)) {
          actualTask.predecessors.add(tasks.get(neighbor));
        }
        copy.removeDependents(node, edgeFilter);
      }
    }
    return result;
  }

  private Graph<E, V> removeSelfDependencies(Graph<E, V> graph) {
    val copy = graph.clone();
    return copy.clone();
  }

  private List<V> collectFrontier(Graph<E, V> copy, Predicate<E> edgeFilter) {
    val results = new ArrayList<V>();
    for (val c : copy.vertexSet()) {
      if (copy.degreeOf(c, edgeFilter) == 0) {
        results.add(c);
      }
    }
    return results;
    //    return copy.vertexSet().stream()
    //        .filter(t -> copy.degreeOf(t, edgeFilter) == 0)
    //        .collect(Collectors.toList());
  }

  static final class MutableSchedule<E, V> implements Schedule<E, V> {

    final List<TaskSet<E, V>> tasks;

    MutableSchedule() {
      tasks = new ArrayList<>();
    }

    private MutableSchedule(List<TaskSet<E, V>> tasks) {
      this.tasks = tasks;
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
    public Schedule<E, V> reverse() {
      val result = new ArrayList<>(tasks);
      Collections.reverse(result);
      return new MutableSchedule<>(result);
    }

    @Override
    public Iterator<TaskSet<E, V>> iterator() {
      return tasks.iterator();
    }
  }
}
