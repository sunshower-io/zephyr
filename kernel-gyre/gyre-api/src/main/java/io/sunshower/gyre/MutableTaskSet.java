package io.sunshower.gyre;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class MutableTaskSet<E, V> implements TaskSet<E, V> {
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
