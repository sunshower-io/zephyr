package io.sunshower.gyre;

import java.util.Stack;
import java.util.function.Predicate;
import lombok.val;

public class ReverseSubgraphTransformation<E, V> implements Transformation<E, V, Graph<E, V>> {
  final V root;

  public ReverseSubgraphTransformation(V root) {
    this.root = root;
  }

  @Override
  public Graph<E, V> apply(Graph<E, V> graph) {
    return apply(graph, EdgeFilters.acceptAll(), EdgeFilters.acceptAll());
  }

  @Override
  public Graph<E, V> apply(Graph<E, V> graph, Predicate<E> edgeFilter, Predicate<V> nodeFilter) {
    val newgraph = graph.createNew();
    val stack = new Stack<Pair<E, V>>();
    stack.push(Pair.of(null, root));
    newgraph.add(root);
    while (!stack.isEmpty()) {
      val current = stack.pop();
      val neighbors = graph.getDependents(current.snd, edgeFilter);
      for (val neighbor : neighbors) {
        val source = graph.getSource(neighbor);
        newgraph.connect(source, current.snd, null);
        stack.push(Pair.of(null, source));
      }
    }
    return newgraph;
  }
}
