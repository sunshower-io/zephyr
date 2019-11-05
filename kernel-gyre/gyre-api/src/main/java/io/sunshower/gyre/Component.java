package io.sunshower.gyre;

import java.util.List;

public interface Component<E, V> {

  boolean isCyclic();

  Pair<E, V> getOrigin();

  List<Pair<E, V>> getElements();
}
