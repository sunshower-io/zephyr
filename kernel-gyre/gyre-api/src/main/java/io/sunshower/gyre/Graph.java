package io.sunshower.gyre;

import java.util.Set;

public interface Graph<E, V> extends Cloneable {

  int edgeCount();
  int vertexCount();

  int size();

  E connect(V source, V target, E edge);

  E disconnect(V source, V target, E edge);

  /**
   * this removes all connections between source and target
   * @param source
   * @param target
   */
  Set<E> disconnect(V source, V target);

  Set<E> getEdges();

  Set<V> getVertices();

  int degreeOf(V vertex);

  V getSource(E edge);

  V getTarget(E edge);



  Set<E> adjacentEdges(V vertex);

  Set<V> neighbors(V vertex);

  boolean containsEdge(V source, V target);


  boolean containsVertex(V vertex);

  boolean add(V vertex);

  boolean remove(V vertex);

  Graph<E, V> clone();
}
