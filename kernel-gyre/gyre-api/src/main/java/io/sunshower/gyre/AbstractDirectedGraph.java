package io.sunshower.gyre;

import lombok.val;

import java.util.*;

public class AbstractDirectedGraph<E, V> implements DirectedGraph<E, V> {

  private final Map<V, Set<Adjacency<E, V>>> adjacencies;

  public AbstractDirectedGraph() {
    adjacencies = createAdjacencyStructure();
  }

  private AbstractDirectedGraph(Map<V, Set<Adjacency<E, V>>> adjacencies) {
    this.adjacencies = adjacencies;
  }

  @Override
  public boolean containsEdge(V source, V target, Direction d) {
    val neighbors = adjacencies.get(source);
    if (neighbors == null || neighbors.isEmpty()) {
      return false;
    }

    for (val neighbor : neighbors) {
      if (d.is(neighbor.directions) && Objects.equals(target, neighbor.target)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Set<E> adjacentEdges(V vertex, Direction direction) {
    val neighbors = adjacencies.get(vertex);
    if (neighbors == null || neighbors.isEmpty()) {
      return Collections.emptySet();
    }
    val results = new HashSet<E>(neighbors.size());
    for (val neighbor : neighbors) {
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
    for (val neighbor : neighbors) {
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
      neighbors = new HashSet<>();
      adjacencies.put(source, neighbors);
    }

    val neighbor = new Adjacency<E, V>(source, target, edge.value);
    neighbor.directions = edge.direction.set(neighbor.directions);
    neighbors.add(neighbor);
    if (!adjacencies.containsKey(target)) {
      adjacencies.put(target, new HashSet<>());
    } else {
      return edge;
    }
    return null;
  }

  @Override
  public Edge<E> disconnect(V source, V target, Edge<E> edge) {
    val neighbors = adjacencies.get(source);
    if (neighbors == null || neighbors.isEmpty()) {
      return null;
    }

    val iter = neighbors.iterator();
    while (iter.hasNext()) {
      val next = iter.next();
      if (edge.direction.is(next.directions) && Objects.equals(edge.value, next.value)) {
        iter.remove();
        return edge;
      }
    }
    return null;
  }

  @Override
  public Set<Edge<E>> disconnect(V source, V target) {
    val neighbors = adjacencies.get(source);
    if (neighbors == null || neighbors.isEmpty()) {
      return Collections.emptySet();
    }

    val result = new HashSet<Edge<E>>(neighbors.size());

    val iter = neighbors.iterator();
    while (iter.hasNext()) {
      val next = iter.next();
      if (Objects.equals(next.target, target)) {
        for (val direction : Direction.values()) {
          if (direction.is(next.directions)) {
            result.add(new Edge<>(next.value, direction));
            iter.remove();
          }
        }
      }
    }
    return result;
  }

  @Override
  public Set<Edge<E>> getEdges() {
    Set<Edge<E>> results = new HashSet<>(adjacencies.size());
    for (val adjs : adjacencies.values()) {
      for (val neighbors : adjs) {
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
    return adjacencies.keySet();
  }

  @Override
  public int degreeOf(V vertex) {
    val neighbors = adjacencies.get(vertex);
    if (neighbors == null || neighbors.isEmpty()) {
      return 0;
    }
    return neighbors.size();
  }

  @Override
  public V getSource(Edge<E> edge) {
    for (val adjacency : adjacencies.entrySet()) {
      for (val actual : adjacency.getValue()) {
        if (edge.direction.is(actual.directions) && Objects.equals(actual.value, edge.value)) {
          return adjacency.getKey();
        }
      }
    }
    return null;
  }

  @Override
  public V getTarget(Edge<E> edge) {
    for (val adjacency : adjacencies.entrySet()) {
      for (val actual : adjacency.getValue()) {
        if (edge.direction.is(actual.directions) && Objects.equals(actual.value, edge.value)) {
          return actual.target;
        }
      }
    }
    return null;
  }

  @Override
  public Set<Edge<E>> adjacentEdges(V vertex) {
    val neighbors = adjacencies.get(vertex);
    if (neighbors == null || neighbors.isEmpty()) {
      return Collections.emptySet();
    }

    val result = new HashSet<Edge<E>>(neighbors.size());
    for (val r : neighbors) {
      for (val d : Direction.values()) {
        if (d.is(r.directions)) {
          result.add(new Edge<>(r.value, d));
        }
      }
    }
    return result;
  }

  @Override
  public Set<V> neighbors(V vertex) {
    val neighbors = adjacencies.get(vertex);
    if (neighbors == null || neighbors.isEmpty()) {
      return Collections.emptySet();
    }

    val result = new HashSet<V>(neighbors.size());
    for (val neighbor : neighbors) {
      result.add(neighbor.target);
    }
    return result;
  }

  @Override
  public boolean containsEdge(V source, V target) {
    val neighbors = adjacencies.get(source);
    if (neighbors == null || neighbors.isEmpty()) {
      return false;
    }
    for (val v : neighbors) {
      if (Objects.equals(target, v.target)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean containsVertex(V vertex) {
    return adjacencies.containsKey(vertex);
  }

  @Override
  public boolean add(V vertex) {
    return adjacencies.put(vertex, new HashSet<>()) != null;
  }

  @Override
  public boolean remove(V vertex) {
    return adjacencies.remove(vertex) != null;
  }

  protected Map<V, Set<Adjacency<E, V>>> createAdjacencyStructure() {
    return new HashMap<>();
  }

  private static final class Adjacency<E, V> {
    final V source;
    final V target;
    final E value;
    byte directions;

    private Adjacency(V source, V target, E value) {
      this.value = value;
      this.directions = 0;
      this.source = source;
      this.target = target;
    }
  }

  public DirectedGraph<E, V> clone() {
    val adjacencyStructure = createAdjacencyStructure();
    for (val adjacencyList : adjacencies.entrySet()) {
      adjacencyStructure.put(adjacencyList.getKey(), new HashSet<>(adjacencyList.getValue()));
    }
    return new AbstractDirectedGraph<>(adjacencyStructure);
  }
}
