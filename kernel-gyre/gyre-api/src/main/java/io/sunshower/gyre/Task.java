package io.sunshower.gyre;

import java.util.Set;

public interface Task<E, V> {
  V getValue();
  Set<E> getEdges();
}
