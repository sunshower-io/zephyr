package io.sunshower.gyre;

import lombok.val;

import java.util.*;

public class AbstractDirectedGraph<E, V> implements DirectedGraph<E, V> {

  private final Map<V, Map<V, Adjacency<E>>> adjacencies;

  public AbstractDirectedGraph() {
    adjacencies = createAdjacencyStructure();
  }

  @Override
  public boolean containsEdge(V source, V target, Direction d) {
    val neighbors = adjacencies.get(source);
    if (neighbors == null || neighbors.isEmpty()) {
      return false;
    }

    val targets = neighbors.get(target);

    if (targets == null) {
      return false;
    }
    return d.is(targets.directions);
  }

  @Override
  public Set<E> adjacentEdges(V vertex, Direction direction) {
    val neighbors = adjacencies.get(vertex);
    if (neighbors == null || neighbors.isEmpty()) {
      return Collections.emptySet();
    }
    val results = new HashSet<E>();
    for (val neighbor : neighbors.values()) {
      if (direction.is(neighbor.directions)) {
        results.add(neighbor.value);
      }
    }
    return results;
  }

  @Override
  public int degreeOf(V vertex, Direction direction) {
    val neighbors = adjacencies.get(vertex);
    if (neighbors == null || neighbors.isEmpty()) {
      return 0;
    }
    int count = 0;
    for (val neighbor : neighbors.values()) {
      if (direction.is(neighbor.directions)) {
        count++;
      }
    }
    return count;
  }

  @Override
  public int edgeCount() {
    int count = 0;
    for (val neighborList : adjacencies.values()) {
      count += neighborList.size();
    }
    return count;
  }

  @Override
  public int vertexCount() {
    return adjacencies.size();
  }

  @Override
  public int size() {
    return vertexCount();
  }

  @Override
  public Edge<E> connect(V source, V target, Edge<E> edge) {

    var neighbors = adjacencies.get(source);
    if (neighbors == null) {
      neighbors = new HashMap<>();
      adjacencies.put(source, neighbors);
    }

    val neighbor = new Adjacency<E>(edge.value);
    neighbor.directions = edge.direction.set(neighbor.directions);
    neighbors.put(target, neighbor);
    if (!adjacencies.containsKey(target)) {
      adjacencies.put(target, new HashMap<>());
    } else {
      return edge;
    }
    return null;
  }

  @Override
  public Edge<E> disconnect(V source, V target, Edge<E> edge) {
    return null;
  }

  @Override
  public Set<Edge<E>> getEdges() {
    Set<Edge<E>> results = new HashSet<>(adjacencies.size());
    for (val adjs : adjacencies.values()) {
      for (val neighbors : adjs.values()) {
        for (val type : Direction.values()) {
          if (type.is(neighbors.directions)) {
            results.add(new Edge<>(neighbors.value, type));
          }
        }
      }
    }
    return results;
  }

  @Override
  public Set<V> getVertices() {
    return null;
  }

  @Override
  public int degreeOf(V vertex) {
    return 0;
  }

  @Override
  public V getSource(Edge<E> edge) {
    return null;
  }

  @Override
  public V getTarget(Edge<E> edge) {
    return null;
  }

  @Override
  public Set<Edge<E>> adjacentEdges(V vertex) {
    return null;
  }

  @Override
  public Set<V> neighbors(V vertex) {
    return null;
  }

  @Override
  public boolean containsEdge(V source, V target) {
    val neighbors = adjacencies.get(source);
    if (neighbors == null || neighbors.isEmpty()) {
      return false;
    }
    return neighbors.containsKey(target);
  }

  @Override
  public boolean containsVertex(V vertex) {
    return false;
  }

  @Override
  public boolean add(V vertex) {
    return false;
  }

  @Override
  public boolean remove(V vertex) {
    return false;
  }

  protected Map<V, Map<V, Adjacency<E>>> createAdjacencyStructure() {
    return new HashMap<>();
  }

  private static final class Adjacency<E> {
    final E value;
    byte directions;

    private Adjacency(E value) {
      this.value = value;
      this.directions = 0;
    }
  }
}
