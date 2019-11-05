package io.sunshower.gyre;

import lombok.AllArgsConstructor;

import java.util.function.Predicate;

public class EdgeFilters {

  static final AcceptAll ACCEPT_ALL_INSTANCE = new AcceptAll();

  @SuppressWarnings("unchecked")
  public static <E> Predicate<E> acceptAll() {
    return (Predicate<E>) ACCEPT_ALL_INSTANCE;
  }

  public static <E> Predicate<DirectedGraph.Edge<E>> directionFilter(DirectedGraph.Direction d) {
    return new DirectionFilter<E>(d);
  }

  public static Predicate<DirectedGraph.Edge<String>> incoming() {
    return directionFilter(DirectedGraph.Direction.Incoming);
  }

  public static Predicate<DirectedGraph.Edge<String>> outgoing() {
    return directionFilter(DirectedGraph.Direction.Outgoing);
  }

  static final class AcceptAll implements Predicate<Object> {
    @Override
    public boolean test(Object o) {
      return true;
    }
  }

  @AllArgsConstructor
  static final class DirectionFilter<E> implements Predicate<DirectedGraph.Edge<E>> {
    final DirectedGraph.Direction direction;

    @Override
    public boolean test(DirectedGraph.Edge<E> edge) {
      return edge.getDirection() == direction;
    }
  }
}
