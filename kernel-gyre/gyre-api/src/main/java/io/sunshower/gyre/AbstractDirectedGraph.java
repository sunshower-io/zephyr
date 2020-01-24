package io.sunshower.gyre;

import java.util.*;
import java.util.function.Predicate;
import lombok.val;

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
  public Graph<Edge<E>, V> createNew() {
    return new AbstractDirectedGraph<>();
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

    val neighbor = new Adjacency<E, V>(source, target, edge != null ? (E) edge.getLabel() : null);
    neighbor.directions = edge != null ? edge.getDirection().set(neighbor.directions) : 1;
    neighbors.add(neighbor);
    if (!adjacencies.containsKey(target)) {
      adjacencies.put(target, new HashSet<>());
    } else {
      return edge;
    }
    return null;
  }

  @Override
  public Set<Edge<E>> disconnect(V source, V target, Predicate<Edge<E>> edgeFilter) {
    val neighbors = adjacencies.get(source);
    if (neighbors == null || neighbors.isEmpty()) {
      return null;
    }

    val results = new HashSet<Edge<E>>();

    val iter = neighbors.iterator();
    while (iter.hasNext()) {
      val next = iter.next();
      if (!Objects.equals(next.target, target)) {
        continue;
      }
      for (val direction : Direction.values()) {
        val edge = new DirectedEdge<>(next.value, direction);
        if (edgeFilter.test(edge)) {
          results.add(edge);
        }
      }
      iter.remove();
    }
    return results;
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
      if (edge.getDirection().is(next.directions) && Objects.equals(edge.getLabel(), next.value)) {
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
            result.add(new DirectedEdge<>(next.value, direction));
            iter.remove();
          }
        }
      }
    }
    return result;
  }

  @Override
  public Set<Edge<E>> edgeSet() {
    Set<Edge<E>> results = new HashSet<>(adjacencies.size());
    for (val adjs : adjacencies.values()) {
      for (val neighbors : adjs) {
        for (val type : Direction.values()) {
          if (type.is(neighbors.directions)) {
            results.add(new DirectedEdge<>(neighbors.value, type));
          }
        }
      }
    }
    return results;
  }

  @Override
  public Set<V> vertexSet() {
    return new HashSet<>(adjacencies.keySet());
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
  public int degreeOf(V vertex, Predicate<Edge<E>> edgeFilter) {
    val neighbors = adjacencies.get(vertex);
    if (neighbors == null || neighbors.isEmpty()) {
      return 0;
    }
    int count = 0;
    for (val neighbor : neighbors) {
      if (edgeFilter.test(neighbor)) {
        count++;
      }
    }
    return count;
  }

  @Override
  @SuppressWarnings("unchecked")
  public V getSource(Edge<E> edge) {
    if (edge instanceof Adjacency) {
      return (V) ((Adjacency) edge).source;
    }
    for (val adjacency : adjacencies.entrySet()) {
      for (val actual : adjacency.getValue()) {
        if (edge.getDirection().is(actual.directions)
            && Objects.equals(actual.value, edge.getLabel())) {
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
        if (edge.getDirection().is(actual.directions)
            && Objects.equals(actual.value, edge.getLabel())) {
          return actual.target;
        }
      }
    }
    return null;
  }

  @Override
  public Set<Edge<E>> adjacentEdges(V vertex, Predicate<Edge<E>> edgeFilter) {
    val neighbors = adjacencies.get(vertex);
    if (neighbors == null || neighbors.isEmpty()) {
      return Collections.emptySet();
    }

    val result = new HashSet<Edge<E>>(neighbors.size());
    for (val r : neighbors) {
      if (!edgeFilter.test(r)) {
        continue;
      }
      for (val d : Direction.values()) {
        if (d.is(r.directions)) {
          result.add(new DirectedEdge<>(r.value, d));
        }
      }
    }
    return result;
  }

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

  public Set<Pair<Edge<E>, V>> neighbors(V vertex, Predicate<Edge<E>> edgeFilter) {
    val neighbors = adjacencies.get(vertex);
    if (neighbors == null || neighbors.isEmpty()) {
      return Collections.emptySet();
    }

    val result = new HashSet<Pair<Edge<E>, V>>(neighbors.size());
    for (val neighbor : neighbors) {
      if (edgeFilter.test(neighbor)) {
        result.add(Pair.of(neighbor, neighbor.target));
      }
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
  public Set<Edge<E>> remove(V vertex, Predicate<Edge<E>> edgeFilter) {
    val result = new HashSet<Edge<E>>();
    if (adjacencies.remove(vertex) != null) {
      for (val adjacency : adjacencies.values()) {
        val adjiter = adjacency.iterator();
        while (adjiter.hasNext()) {
          val next = adjiter.next();
          if (Objects.equals(next.target, vertex) && edgeFilter.test(next)) {
            result.add(next);
            adjiter.remove();
          }
        }
      }
    }
    return result;
  }

  @Override
  public Set<Pair<V, Edge<E>>> removeDependents(V vertex, Predicate<Edge<E>> edgeFilter) {

    Set<Pair<V, Edge<E>>> results = new HashSet<>();
    for (val adjacency : adjacencies.values()) {
      val adjiter = adjacency.iterator();
      while (adjiter.hasNext()) {
        val next = adjiter.next();
        if (Objects.equals(next.target, vertex)) {
          results.add(Pair.of(next.target, next));
          adjiter.remove();
        }
      }
    }
    return results;
  }

  @Override
  public Collection<Edge<E>> getDependents(V vertex, Predicate<Edge<E>> edgeFilter) {
    List<Edge<E>> results = new ArrayList<>();
    for (val adjacency : adjacencies.values()) {
      val adjiter = adjacency.iterator();
      while (adjiter.hasNext()) {
        val next = adjiter.next();
        if (Objects.equals(next.target, vertex)) {
          results.add(next);
        }
      }
    }
    return results;
  }

  @Override
  public void delete(V node) {
    adjacencies.remove(node);
  }
  /**
   * this method removes a vertex from the graph, including from all of the adjacency-lists that
   * vertex appears in
   */
  @Override
  public boolean remove(V vertex) {
    if (adjacencies.remove(vertex) != null) {
      for (val adjacency : adjacencies.values()) {
        val adjiter = adjacency.iterator();
        while (adjiter.hasNext()) {
          val next = adjiter.next();
          if (Objects.equals(next.target, vertex)) {
            adjiter.remove();
          }
        }
      }
    }
    return false;
  }

  public DirectedGraph<E, V> clone() {
    val adjacencyStructure = createAdjacencyStructure();
    for (val adjacencyList : adjacencies.entrySet()) {
      adjacencyStructure.put(adjacencyList.getKey(), new HashSet<>(adjacencyList.getValue()));
    }
    return new AbstractDirectedGraph<>(adjacencyStructure);
  }

  @Override
  public boolean isEmpty() {
    return size() == 0;
  }

  protected Map<V, Set<Adjacency<E, V>>> createAdjacencyStructure() {
    return new HashMap<>();
  }

  private static final class Adjacency<E, V> implements Edge<E> {
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

    @Override
    public E getLabel() {
      return value;
    }

    @Override
    public Direction getDirection() {
      if (Direction.Incoming.is(directions)) {
        return Direction.Incoming;
      }
      return Direction.Outgoing;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof DirectedGraph.Edge)) return false;

      Edge<?> edge = (Edge<?>) o;

      if (value != null ? !value.equals(edge.getLabel()) : edge.getLabel() != null) return false;
      return getDirection() == edge.getDirection();
    }

    @Override
    public int hashCode() {
      int result = value != null ? value.hashCode() : 0;
      result = 31 * result + getDirection().hashCode();
      return result;
    }

    @Override
    public String toString() {
      return String.format("E[%s:%s]", value, getDirection());
    }
  }

  @Override
  public String toString() {
    return new GraphWriter<Edge<E>, V>().write(this);
  }
}
