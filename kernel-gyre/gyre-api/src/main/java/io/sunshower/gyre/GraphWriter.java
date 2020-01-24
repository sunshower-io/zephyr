package io.sunshower.gyre;

import java.util.ArrayList;
import lombok.val;

public class GraphWriter<E, V> {

  public String write(Graph<E, V> graph) {
    val result = new StringBuilder();
    val roots = new ArrayList<V>();
    for (val node : graph.vertexSet()) {
      if (graph.degreeOf(node, EdgeFilters.acceptAll()) == 0) {
        roots.add(node);
      }
    }

    for (val root : roots) {
      doWrite(root, graph, result, new StringBuilder(), true);
    }
    return result.toString();
  }

  private void doWrite(
      V root, Graph<E, V> graph, StringBuilder result, StringBuilder indent, boolean last) {
    val neighbors = graph.getDependents(root, EdgeFilters.acceptAll());
    val niter = neighbors.iterator();
    for (int i = 0; i < neighbors.size(); i++) {
      val next = niter.next();
      if (next != root) {
        doWrite(graph.getSource(next), graph, result, indent, i == neighbors.size() - 1);
      }
    }
    result.append(indent);
    if (last) {
      result.append("\\--");
      indent.append("\t");
    } else {
      result.append("|-");
      indent.append("|\t");
    }
    result.append(root).append("\n");
  }
}
