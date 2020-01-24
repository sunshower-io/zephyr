package io.sunshower.gyre;

import java.util.List;
import java.util.function.Predicate;

public interface Partition<E, V> {

  boolean isCyclic();

  List<Component<E, V>> getElements();

  List<Component<E, V>> getElements(Predicate<Component<E, V>> filter);
}
