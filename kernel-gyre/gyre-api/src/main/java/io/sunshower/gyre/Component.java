package io.sunshower.gyre;

import java.util.List;

public interface Component<E, V> {

  int size();

  boolean isCyclic();

  Pair<E, V> getOrigin();

  List<Pair<E, V>> getElements();
}
