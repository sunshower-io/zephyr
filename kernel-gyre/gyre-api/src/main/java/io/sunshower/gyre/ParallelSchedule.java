package io.sunshower.gyre;

import java.util.List;

public interface ParallelSchedule<E, V> extends Iterable<TaskSet<E, V>> {
  List<TaskSet<E, V>> getTasks();

  int size();

  TaskSet<E, V> get(int i);
}
