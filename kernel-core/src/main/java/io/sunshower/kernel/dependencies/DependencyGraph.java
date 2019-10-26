package io.sunshower.kernel.dependencies;

import io.sunshower.kernel.Coordinate;
import io.sunshower.kernel.Dependency;
import io.sunshower.kernel.Module;
import java.util.*;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.val;
import org.jetbrains.annotations.NotNull;

public class DependencyGraph implements Iterable<Module> {

  private final Map<Coordinate, DependencyNode> adjacencies;

  private DependencyGraph(@NonNull final Map<Coordinate, DependencyNode> adjacencies) {
    this.adjacencies = adjacencies;
  }

  public int size() {
    return adjacencies.size();
  }

  @NotNull
  @Override
  public Iterator<Module> iterator() {
    return adjacencies.values().stream().map(t -> t.module).iterator();
  }

  @SuppressWarnings({"PMD.DataflowAnomalyAnalysis", "PMD.AvoidInstantiatingObjectsInLoops"})
  public static DependencyGraph create(Collection<Module> modules) {
    val result = new LinkedHashMap<Coordinate, DependencyNode>(modules.size());
    val links = buildCoordinateLinks(modules);

    for (val module : modules) {
      val node = new DependencyNode(module, module.getCoordinate(), new ArrayList<>());
      computeDependencies(links, module, node);
      result.put(module.getCoordinate(), node);
    }
    return new DependencyGraph(result);
  }

  @SuppressWarnings({
    "PMD.DataflowAnomalyAnalysis",
    "PMD.AvoidInstantiatingObjectsInLoops",
    "PMD.UnusedPrivateMethod"
  })
  private static void computeDependencies(
      Map<Coordinate, Module> links, Module module, DependencyNode node) {
    for (val dependency : module.getDependencies()) {
      val depmod = links.get(dependency.getCoordinate());
      if (depmod == null) {
        throw new UnsatisfiedDependencyException(module, dependency);
      }
      // this is ok because we actually don't know if we can build out the full dependency structure
      // yet (i.e. the graph doesn't have a topological order).  This is computed later
      val depnode = new DependencyNode(depmod, dependency.getCoordinate(), Collections.emptyList());
      node.addDependency(depnode);
    }
  }

  private static Map<Coordinate, Module> buildCoordinateLinks(Collection<Module> modules) {
    val coordinateLinks = new HashMap<Coordinate, Module>();
    for (val module : modules) {
      coordinateLinks.put(module.getCoordinate(), module);
    }
    return coordinateLinks;
  }

  public Module get(Coordinate dependency) {
    val dep = adjacencies.get(dependency);
    if (dep == null) {
      throw new UnsatisfiedDependencyException(
          null, new Dependency(Dependency.Type.Library, dependency));
    }
    return dep.module;
  }

  public DependencyGraph add(Module module) {
    val modules =
        adjacencies
            .values()
            .stream()
            .map(t -> t.module)
            .collect(Collectors.toCollection(ArrayList::new));
    modules.add(module);
    return DependencyGraph.create(modules);
  }

  public List<Module> getDependants(Coordinate coordinate) {
    return adjacencies
        .values()
        .stream()
        .filter(t -> t.dependsOn(coordinate))
        .map(t -> t.module)
        .collect(Collectors.toList());
  }

  public DependencyGraph remove(Coordinate coordinate) {
    val modules =
        adjacencies
            .values()
            .stream()
            .filter(t -> !t.coordinate.equals(coordinate))
            .map(t -> t.module)
            .collect(Collectors.toCollection(ArrayList::new));
    return DependencyGraph.create(modules);
  }
}
