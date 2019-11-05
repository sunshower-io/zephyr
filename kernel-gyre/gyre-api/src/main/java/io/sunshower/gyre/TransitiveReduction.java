package io.sunshower.gyre;

import lombok.val;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.function.Predicate;

public class TransitiveReduction<E, V> implements Transformation<E, V, Graph<E, V>> {

  @Override
  public Graph<E, V> apply(Graph<E, V> graph) {
    return apply(graph, EdgeFilters.acceptAll(), NodeFilters.acceptAll());
  }

  @Override
  public Graph<E, V> apply(Graph<E, V> graph, Predicate<E> edgeFilter, Predicate<V> nodeFilter) {
    return compute(graph, edgeFilter, nodeFilter);
  }

  private Graph<E, V> compute(
      Graph<E, V> source, Predicate<E> edgeFilter, Predicate<V> nodeFilter) {
    val result = source.clone();

    val vertices = createIndexTable(source, nodeFilter);

    val n = vertices.size();

    val original = new BitSet[n];

    for (int i = 0; i < n; i++) {
      original[i] = new BitSet(n);
    }

    val edges = source.edgeSet();

    for (val edge : edges) {
      val fst = source.getSource(edge);
      val snd = source.getTarget(edge);

      int fstidx = vertices.indexOf(fst);
      int sndidx = vertices.indexOf(snd);

      original[fstidx].set(sndidx);
    }

    val pathMatrix = reduce(transform(original));

    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        if (!pathMatrix[i].get(j)) {
          result.disconnect(vertices.get(i), vertices.get(j), edgeFilter);
        }
      }
    }
    return result;
  }

  private List<V> createIndexTable(Graph<E, V> source, Predicate<V> nodeFilter) {
    val vertices = new ArrayList<V>(source.size());
    for (val vertex : source.vertexSet()) {
      if (nodeFilter.test(vertex)) {
        vertices.add(vertex);
      }
    }
    return vertices;
  }

  private BitSet[] reduce(BitSet[] matrix) {
    val size = matrix.length;

    for (int j = 0; j < size; j++) {
      for (int i = 0; i < size; i++) {
        if (matrix[i].get(j)) {
          for (int k = 0; k < size; k++) {
            if (matrix[j].get(k)) {
              matrix[i].set(k, false);
            }
          }
        }
      }
    }
    return matrix;
  }

  private BitSet[] transform(BitSet[] matrix) {

    int size = matrix.length;

    for (int i = 0; i < size; i++) {
      for (int j = 0; j < size; j++) {
        if (i != j) {
          if (matrix[j].get(i)) {
            for (int k = 0; k < size; k++) {
              if (!matrix[j].get(k)) {
                matrix[j].set(k, matrix[i].get(k));
              }
            }
          }
        }
      }
    }
    return matrix;
  }
}
