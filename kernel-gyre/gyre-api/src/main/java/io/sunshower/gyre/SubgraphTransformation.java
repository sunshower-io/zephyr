package io.sunshower.gyre;

import java.util.Stack;
import java.util.function.Predicate;
import lombok.val;

public class SubgraphTransformation<E, V> implements Transformation<E, V, Graph<E, V>> {

  final V root;

  public SubgraphTransformation(V root) {
    this.root = root;
  }

  @Override
  public Graph<E, V> apply(Graph<E, V> graph) {
    return apply(graph, EdgeFilters.acceptAll(), NodeFilters.acceptAll());
  }

  @Override
  public Graph<E, V> apply(Graph<E, V> graph, Predicate<E> edgeFilter, Predicate<V> nodeFilter) {
    Graph<E, V> newGraph = graph.createNew();
    final Stack<Pair<E, V>> stack = new Stack<>();
    stack.push(Pair.of(null, root));
    newGraph.add(root);
    while (!stack.isEmpty()) {
      val current = stack.pop();
      val neighbors = graph.neighbors(current.snd, edgeFilter);
      for (val neighbor : neighbors) {
        newGraph.connect(current.snd, neighbor.snd, neighbor.fst);
        stack.push(neighbor);
      }
    }
    return newGraph;
  }
}
