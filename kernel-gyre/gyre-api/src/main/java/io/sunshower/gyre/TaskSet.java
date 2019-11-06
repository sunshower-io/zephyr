package io.sunshower.gyre;

import java.util.List;

public interface TaskSet<E, V> {
  List<Task<E, V>> getTasks();

  int size();
}
