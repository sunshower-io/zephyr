package io.sunshower.gyre;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.val;

final class LabeledTask<E, V> implements Task<E, V> {
  final V value;
  final Set<E> edges;
  final Scope scope;
  final Set<Task<E, V>> predecessors;

  LabeledTask(V value, Set<E> edges, Set<Task<E, V>> predecessors) {
    this.value = value;
    this.edges = edges;
    this.scope = new PredecessorScanningTaskScope();
    this.predecessors = predecessors;
  }

  @Override
  public V getValue() {
    return value;
  }

  @Override
  public Scope getScope() {
    return scope;
  }

  @Override
  public Set<Task<E, V>> getPredecessors() {
    return predecessors;
  }

  @Override
  public Set<E> getEdges() {
    return edges;
  }

  class PredecessorScanningTaskScope implements Scope {
    final Map<String, Object> values;

    PredecessorScanningTaskScope() {
      values = new HashMap<>(2);
    }

    @Override
    public <T> void set(String name, T value) {
      values.put(name, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String name) {
      val result = (T) values.get(name);
      if (result == null) {
        for (val predecessor : predecessors) {
          val presult = predecessor.getScope().<T>get(name);
          if (presult != null) {
            return presult;
          }
        }
      }
      return result;
    }

    @Override
    public synchronized <E> E computeIfAbsent(String name, E o) {
      val c = get(name);
      if (c == null) {
        values.put(name, o);
        return o;
      } else {
        return (E) c;
      }
    }
  }
}
