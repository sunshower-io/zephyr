package io.sunshower.gyre;

import lombok.AllArgsConstructor;
import lombok.val;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.lang.Math.min;
import static java.lang.String.format;

public class StronglyConnectedComponents<E, V> implements Transformation<E, V, Partition<E, V>> {
  @Override
  public Partition<E, V> apply(Graph<E, V> graph) {
    return apply(graph, EdgeFilters.acceptAll(), NodeFilters.acceptAll());
  }

  @Override
  public Partition<E, V> apply(
      Graph<E, V> graph, Predicate<E> edgeFilter, Predicate<V> nodeFilter) {

    val links = new HashMap<V, Link>();
    val component = new Stack<Link<E, V>>();
    val partition = new MutablePartition<E, V>();

    return doCompute(graph, component, partition, links, edgeFilter, nodeFilter, 0);
  }

  private Partition<E, V> doCompute(
      Graph<E, V> graph,
      Stack<Link<E, V>> component,
      MutablePartition<E, V> partition,
      HashMap<V, Link> links,
      Predicate<E> edgeFilter,
      Predicate<V> nodeFilter,
      int idx) {
    for (val vertex : graph.vertexSet()) {
      if (!links.containsKey(vertex)) {
        idx =
            computeComponent(
                idx, null, vertex, graph, component, links, edgeFilter, nodeFilter, partition);
      }
    }
    return partition;
  }

  private int computeComponent(
      int idx,
      E edge,
      V vertex,
      Graph<E, V> graph,
      Stack<Link<E, V>> component,
      HashMap<V, Link> links,
      Predicate<E> edgeFilter,
      Predicate<V> nodeFilter,
      MutablePartition<E, V> partition) {

    var result = idx;
    val link = configureLink(edge, vertex, component, links, result);
    result = result + 1;

    for (val ve : graph.neighbors(vertex, edgeFilter)) {
      val e = ve.fst;
      val neighbor = ve.snd;
      if (!links.containsKey(neighbor)) {
        result =
            computeComponent(
                result, e, neighbor, graph, component, links, edgeFilter, nodeFilter, partition);
        link.link = min(links.get(vertex).link, links.get(neighbor).link);
      } else {
        val prospect = links.get(neighbor);
        link.link = min(link.link, prospect.index);
      }
    }

    if (link.index == link.link) {
      partition.add(extractComponent(edge, vertex, component));
    }

    return result;
  }

  private Component<E, V> extractComponent(E edge, V vertex, Stack<Link<E, V>> currentComponent) {

    val component = new SCComponent<>(edge, vertex);
    V current;
    do {
      val head = currentComponent.pop();
      current = head.vertex;
      component.add(head.edge, current);
    } while (!(currentComponent.isEmpty() || Objects.equals(vertex, current)));

    return component;
  }

  private Link<E, V> configureLink(
      E edge, V vertex, Stack<Link<E, V>> component, HashMap<V, Link> links, int idx) {
    val link = new Link<E, V>(idx, idx, edge, vertex);
    component.push(link);
    links.put(vertex, link);
    return link;
  }

  @AllArgsConstructor
  private static final class Link<E, V> {
    private int link;
    private int index;
    private final E edge;
    private final V vertex;
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
