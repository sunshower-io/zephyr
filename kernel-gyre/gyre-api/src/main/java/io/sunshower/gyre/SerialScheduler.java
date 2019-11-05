package io.sunshower.gyre;

import lombok.AllArgsConstructor;
import lombok.val;

import java.util.Collections;
import java.util.function.Predicate;

@AllArgsConstructor
public class SerialScheduler<E, V> implements Transformation<E, V, Schedule<E, V>> {

  final Transformation<E, V, Partition<E, V>> sort;

  public SerialScheduler() {
    this(new StronglyConnectedComponents<>());
  }

  @Override
  public Schedule<E, V> apply(Graph<E, V> graph) {
    return toSchedule(sort.apply(graph));
  }

  @Override
  public Schedule<E, V> apply(Graph<E, V> graph, Predicate<E> edgeFilter, Predicate<V> nodeFilter) {
    return toSchedule(sort.apply(graph, edgeFilter, nodeFilter));
  }

  private Schedule<E, V> toSchedule(Partition<E, V> partition) {
    val schedule = new ParallelScheduler.MutableSchedule<E, V>();
    for (val component : partition.getElements()) {
      if (component.isCyclic()) {
        throw new IllegalArgumentException("how'd we get here?  Cycle at " + component);
      }
      for (val el : component.getElements()) {

        val taskSet =
            new ParallelScheduler.MutableTaskSet<E, V>(
                new ParallelScheduler.LabeledTask<>(el.snd, Collections.singleton(el.fst)));
        schedule.tasks.add(taskSet);
      }
    }
    return schedule;
  }
}
