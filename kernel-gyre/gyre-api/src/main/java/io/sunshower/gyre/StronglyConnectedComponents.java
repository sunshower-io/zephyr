package io.sunshower.gyre;

import static java.lang.Math.min;
import static java.lang.String.format;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.val;

public class StronglyConnectedComponents<E, V> implements Transformation<E, V, Partition<E, V>> {
  @Override
  public Partition<E, V> apply(Graph<E, V> graph) {
    return apply(graph, EdgeFilters.acceptAll(), NodeFilters.acceptAll());
  }

  @Override
  public Partition<E, V> apply(
      Graph<E, V> graph, Predicate<E> edgeFilter, Predicate<V> nodeFilter) {

    val links = new HashMap<V, Link<E, V>>();
    val component = new Stack<Link<E, V>>();
    val partition = new MutablePartition<E, V>();
    val considering = new HashSet<V>();

    doCompute(graph, component, partition, links, considering, edgeFilter);
    return partition;
  }

  private void doCompute(
      Graph<E, V> graph,
      Stack<Link<E, V>> component,
      MutablePartition<E, V> partition,
      Map<V, Link<E, V>> links,
      Set<V> considering,
      Predicate<E> edgeFilter) {
    var index = 0;

    for (val vertex : graph.vertexSet()) {
      if (!links.containsKey(vertex)) {
        computeComponent(
            null, vertex, index, graph, links, component, considering, edgeFilter, partition);
      }
    }
  }

  private int computeComponent(
      E edge,
      V vertex,
      int index,
      Graph<E, V> graph,
      Map<V, Link<E, V>> links,
      Stack<Link<E, V>> stack,
      Set<V> considering,
      Predicate<E> edgeFilter,
      MutablePartition<E, V> partition) {
    val link = new Link<E, V>(index, index, edge, vertex);
    links.put(vertex, link);
    index = index + 1;
    stack.push(link);
    considering.add(vertex);

    for (val ve : graph.neighbors(vertex, edgeFilter)) {
      val neighbor = ve.snd;
      if (!links.containsKey(neighbor)) {
        index =
            computeComponent(
                ve.fst, neighbor, index, graph, links, stack, considering, edgeFilter, partition);
        link.link = min(link.link, links.get(neighbor).link);
      } else if (considering.contains(neighbor)) {
        link.link = min(link.link, links.get(neighbor).index);
      }
    }

    if (link.link == link.index) {
      val scc = new SCComponent<E, V>(edge, vertex);
      V w;
      do {
        val wlink = stack.pop();
        w = wlink.vertex;
        considering.remove(w);
        scc.add(wlink.edge, w);
      } while (!Objects.equals(w, vertex));
      partition.add(scc);
    }
    return index;
  }

  private static final class Link<E, V> {
    private int link;
    private int index;
    private final E edge;
    private final V vertex;

    public Link(int link, int index, E edge, V vertex) {
      this.edge = edge;
      this.link = link;
      this.index = index;
      this.vertex = vertex;
    }
  }

  private static final class SCComponent<E, V> implements Component<E, V> {
    final E edge;
    final V vertex;

    final List<Pair<E, V>> elements;

    private SCComponent(E edge, V vertex) {
      this.edge = edge;
      this.vertex = vertex;
      elements = new ArrayList<>();
    }

    void add(E edge, V vertex) {
      elements.add(Pair.of(edge, vertex));
    }

    @Override
    public int size() {
      return elements.size();
    }

    @Override
    public boolean isCyclic() {
      return elements.size() > 1;
    }

    @Override
    public Pair<E, V> getOrigin() {
      return Pair.of(edge, vertex);
    }

    @Override
    public List<Pair<E, V>> getElements() {
      val result = new ArrayList<Pair<E, V>>(elements);
      Collections.reverse(result);
      return result;
    }

    @Override
    public String toString() {
      val result =
          format(
              "%s Component[origin={origin: e=%s, v=%s}(\n",
              isCyclic() ? "Cyclic" : "Acyclic", edge, vertex);
      val sbuilder = new StringBuilder(result);
      val els = getElements();
      for (int i = 0; i < els.size(); i++) {
        String prefix = "  ".repeat(i + 1);
        val el = els.get(i);
        sbuilder.append(format("%s -> to (v=%s via e=%s)\n", prefix, el.snd, el.fst));
      }
      return sbuilder.append(")").toString();
    }
  }

  private static final class MutablePartition<E, V> implements Partition<E, V> {
    private final List<Component<E, V>> components;

    MutablePartition() {
      components = new ArrayList<>();
    }

    void add(Component<E, V> component) {
      components.add(component);
    }

    @Override
    public boolean isCyclic() {
      for (val c : components) {
        if (c.isCyclic()) {
          return true;
        }
      }
      return false;
    }

    @Override
    public List<Component<E, V>> getElements() {
      return components;
    }

    @Override
    public List<Component<E, V>> getElements(Predicate<Component<E, V>> filter) {
      return components.stream().filter(filter).collect(Collectors.toList());
    }
  }
}
