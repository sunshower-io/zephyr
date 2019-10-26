package io.sunshower.kernel.dependencies;

import io.sunshower.kernel.Coordinate;
import io.sunshower.kernel.Module;
import java.util.*;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;

@AllArgsConstructor
public final class ModuleCycleDetector {

  private final DependencyGraph modules;

  public static ModuleCycleDetector newDetector(Collection<Module> asList) {
    return newDetector(DependencyGraph.create(asList));
  }

  public static ModuleCycleDetector newDetector(DependencyGraph prospective) {
    return new ModuleCycleDetector(prospective);
  }

  @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
  public Components compute() {
    val stack = new Stack<Link>();
    val results = new ArrayList<Component>();
    val links = new HashMap<Coordinate, Link>();

    var index = 0;
    for (val module : modules) {
      if (!links.containsKey(module.getCoordinate())) {
        index = computeComponents(module, links, stack, results, index);
      }
    }
    return new Components(results);
  }

  @SuppressWarnings({"PMD.AvoidReassigningParameters", "PMD.UnusedPrivateMethod"})
  private int computeComponents(
      Module module,
      Map<Coordinate, Link> links,
      Stack<Link> stack,
      List<Component> results,
      int index) {

    val id = module.getCoordinate();
    val link = new Link(id, index, index, module);
    index = index + 1;
    stack.push(link);
    links.put(id, link);

    val dependencies = module.getDependencies();

    for (val dep : dependencies) {
      val dependency = dep.getCoordinate();
      if (!links.containsKey(dependency)) {
        val modDep = modules.get(dependency);
        index = computeComponents(modDep, links, stack, results, index);
        link.link = Math.min(links.get(link.id).link, links.get(dependency).link);
      } else {
        val nlink = links.get(dependency);
        link.link = Math.min(link.link, nlink.index);
      }
    }

    if (link.index == link.link) {
      val component = new Component(module);
      Module current;
      do {
        current = stack.pop().module;
        component.members.add(current);
      } while (!(stack.isEmpty() || module.equals(current)));

      results.add(component);
    }
    return index;
  }

  @SuppressWarnings("PMD.AvoidFieldNameMatchingTypeName")
  public static final class Components {
    final List<Component> components;

    Components(List<Component> components) {
      this.components = components;
    }

    public List<Module> getTopologicalOrdering() {
      if (hasCycle()) {
        throw new IllegalArgumentException(
            "Error: this dependency graph has at least one cycle--can't compute its topological order");
      }

      val items = components.stream().map(Component::getRoot).collect(Collectors.toList());
      Collections.reverse(items);
      return items;
    }

    void addComponent(Component component) {
      components.add(component);
    }

    public boolean hasCycle() {
      return components.stream().anyMatch(Component::isCyclic);
    }

    public List<Component> getCycles() {
      return components.stream().filter(Component::isCyclic).collect(Collectors.toList());
    }
  }

  public static final class Component {
    @Getter private final Module root;
    @Getter private final List<Module> members = new ArrayList<>();

    private Component(@NonNull Module root) {
      this.root = root;
    }

    public boolean isCyclic() {
      return size() > 1;
    }

    public int size() {
      return members.size();
    }
  }

  @AllArgsConstructor
  @SuppressWarnings("PMD.AvoidFieldNameMatchingTypeName")
  static final class Link {

    Coordinate id;
    int index;
    int link;
    Module module;
  }
}
