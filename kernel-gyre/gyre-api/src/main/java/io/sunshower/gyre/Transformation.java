package io.sunshower.gyre;

import java.util.function.Predicate;

public interface Transformation<E, V, T> {

  T apply(Graph<E, V> graph);


  T apply(Graph<E, V> graph, Predicate<E> edgeFilter, Predicate<V> nodeFilter);


  default T apply(Graph<E, V> graph, Predicate<E> edgeFilter) {
    return apply(graph, edgeFilter, NodeFilters.acceptAll());
  }


}
