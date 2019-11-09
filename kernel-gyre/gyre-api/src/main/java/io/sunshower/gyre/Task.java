package io.sunshower.gyre;

import java.util.Set;

public interface Task<E, V> {
  V getValue();

  TaskScope getScope();

  Set<Task<E, V>> getPredecessors();

  Set<E> getEdges();

  interface TaskScope {

  }
}
