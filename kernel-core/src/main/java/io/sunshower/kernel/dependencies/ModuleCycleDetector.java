package io.sunshower.kernel.dependencies;

import io.sunshower.kernel.Coordinate;
import io.sunshower.kernel.Module;
import java.util.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;

@AllArgsConstructor
public class ModuleCycleDetector {

  private final DependencyGraph modules;

  public static ModuleCycleDetector newDetector(Collection<Module> asList) {
    return new ModuleCycleDetector(DependencyGraph.create(asList));
  }

  @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
  public List<Component> compute() {
    val stack = new Stack<Link>();
    val results = new ArrayList<Component>();
    val links = new HashMap<Coordinate, Link>();

    var index = 0;
    for (val module : modules) {
      if (!links.containsKey(module.getCoordinate())) {
        index = compute(module, links, stack, results, index);
      }
    }

    return results;
  }

  @SuppressWarnings({"PMD.AvoidReassigningParameters", "PMD.UnusedPrivateMethod"})
  private int compute(
      Module module,
      Map<Coordinate, Link> links,
      Stack<Link> stack,
      List<Component> results,
      int index) {

    val id = module.getCoordinate();
    val link = new Link(id, index, index, module, true);
    index = index + 1;
    stack.push(link);
    links.put(id, link);

    val dependencies = module.getDependencies();

    for (val dependency : dependencies) {
      if (!links.containsKey(dependency)) {
        val modDep = modules.get(dependency);
        index = compute(modDep, links, stack, results, index);
        link.link = Math.min(links.get(link.id).link, links.get(dependency).link);
      } else {
        val nlink = links.get(dependency);
        if (nlink.considering) {
          link.link = Math.min(link.link, nlink.index);
        }
      }
    }

    if (link.index == link.link) {
      val component = new Component(module);
      Module current;
      do {
        current = stack.pop().module;
        component.members.add(current);
      } while (!(stack.isEmpty() || module.equals(current)));

      if (component.isCyclic()) {
        results.add(component);
      }
    }
    return index;
  }

  public static class Component {
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
    boolean considering;
  }
}
