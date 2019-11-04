package io.sunshower.gyre;

import lombok.val;

import java.util.ArrayList;
import java.util.BitSet;

public class TransitiveReduction<E, V> {

  public Graph<E, V> compute(Graph<E, V> source) {
    val result = source.clone();

    val vertices = new ArrayList<V>(source.getVertices());
    val n = vertices.size();

    val original = new BitSet[n];

    for (int i = 0; i < original.length; i++) {
      original[i] = new BitSet(n);
    }

    val edges = source.getEdges();

    for (val edge : edges) {
      val fst = source.getSource(edge);
      val snd = source.getTarget(edge);

      int fstidx = vertices.indexOf(fst);
      int sndidx = vertices.indexOf(snd);

      original[fstidx].set(sndidx);
    }

    val pathMatrix = reduce(transform(original));


    for(int i = 0; i < n; i++) {
      for(int j = 0; j < n; j++) {
        if(!pathMatrix[i].get(j)) {
          result.disconnect(vertices.get(i), vertices.get(j));
        }
      }
    }
    return result;
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
