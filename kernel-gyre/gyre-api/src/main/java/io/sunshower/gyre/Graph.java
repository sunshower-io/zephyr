package io.sunshower.gyre;

import java.util.Set;
import java.util.function.Predicate;

public interface Graph<E, V> extends Cloneable {

  int edgeCount();

  int vertexCount();

  int size();

  E connect(V source, V target, E edge);

  E disconnect(V source, V target, E edge);

  Set<E> disconnect(V source, V target, Predicate<E> edgeFilter);

  /**
   * this removes all connections between source and target
   *
   * @param source
   * @param target
   */
  Set<E> disconnect(V source, V target);

  Set<E> edgeSet();

  Set<V> vertexSet();

  int degreeOf(V vertex);

  int degreeOf(V vertex, Predicate<E> edgeFilter);

  V getSource(E edge);

  V getTarget(E edge);

  default Set<E> adjacentEdges(V vertex) {
    return adjacentEdges(vertex, EdgeFilters.acceptAll());
  }

  Set<E> adjacentEdges(V vertex, Predicate<E> edgeFilter);

  Set<V> neighbors(V vertex);

  Set<Pair<E, V>> neighbors(V vertex, Predicate<E> edgeFilter);

  boolean containsEdge(V source, V target);

  boolean containsVertex(V vertex);

  boolean add(V vertex);

  boolean remove(V vertex);

  Graph<E, V> clone();

  boolean isEmpty();

  Set<E> remove(V v, Predicate<E> edgeFilter);
}
