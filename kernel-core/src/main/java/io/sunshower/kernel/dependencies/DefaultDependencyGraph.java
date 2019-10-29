package io.sunshower.kernel.dependencies;

import io.sunshower.kernel.Coordinate;
import io.sunshower.kernel.Dependency;
import io.sunshower.kernel.Module;
import io.sunshower.kernel.UnsatisfiedDependencyException;
import java.util.*;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.val;

@SuppressWarnings({
  "PMD.DataflowAnomalyAnalysis",
  "PMD.AvoidInstantiatingObjectsInLoops",
  "PMD.UnusedPrivateMethod"
})
public final class DefaultDependencyGraph implements DependencyGraph {

  private final Map<Coordinate, DependencyNode> adjacencies;

  private DefaultDependencyGraph(@NonNull final Map<Coordinate, DependencyNode> adjacencies) {
    this.adjacencies = adjacencies;
  }

  public DefaultDependencyGraph() {
    adjacencies = new HashMap<>();
  }

  @Override
  public int size() {
    return adjacencies.size();
  }

  @Override
  public Iterator<Module> iterator() {
    return adjacencies.values().stream().map(t -> t.module).iterator();
  }

  @SuppressWarnings({"PMD.DataflowAnomalyAnalysis"})
  public static DefaultDependencyGraph create(Collection<Module> modules) {
    val result = new LinkedHashMap<Coordinate, DependencyNode>(modules.size());
    val links = buildCoordinateLinks(modules);

    for (val module : modules) {
      val node = new DependencyNode(module, module.getCoordinate(), new ArrayList<>());
      computeDependencies(links, module, node);
      result.put(module.getCoordinate(), node);
    }
    return new DefaultDependencyGraph(result);
  }

  @SuppressWarnings({"PMD.DataflowAnomalyAnalysis", "PMD.UnusedPrivateMethod"})
  private static void computeDependencies(
      Map<Coordinate, Module> links, Module module, DependencyNode node) {
    for (val dependency : module.getDependencies()) {
      val depmod = links.get(dependency.getCoordinate());
      if (depmod == null) {
        throw new UnsatisfiedDependencyException(module, Collections.singleton(dependency));
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

  @Override
  public Module get(Coordinate dependency) {
    val dep = adjacencies.get(dependency);
    if (dep == null) {
      throw new UnsatisfiedDependencyException(
          null, Collections.singleton(new Dependency(Dependency.Type.Library, dependency)));
    }
    return dep.module;
  }

  @Override
  public void add(@NonNull Module module) {
    val modDeps = module.getDependencies();
    val dependencies = new ArrayList<DependencyNode>(modDeps.size());
    val coordinate = module.getCoordinate();
    //    for (val dependency : modDeps) {
    //      val dependentModule = get(dependency.getCoordinate());
    //      dependencies.add(
    //          new DependencyNode(dependentModule, dependency.getCoordinate(),
    // Collections.emptyList()));
    //    }
    val toAdd = new DependencyNode(module, coordinate, dependencies);
    adjacencies.put(coordinate, toAdd);
  }

  @Override
  public void remove(Module module) {}

  @Override
  public Set<Module> getDependants(Coordinate coordinate) {
    return adjacencies
        .values()
        .stream()
        .filter(t -> t.dependsOn(coordinate))
        .map(t -> t.module)
        .collect(Collectors.toUnmodifiableSet());
  }

  @Override
  public Set<Module> getDependencies(Coordinate coordinate) {
    return get(coordinate)
        .getDependencies()
        .stream()
        .map(t -> get(t.getCoordinate()))
        .collect(Collectors.toSet());
  }

  @Override
  public boolean contains(Coordinate coordinate) {
    return adjacencies.containsKey(coordinate);
  }

  @Override
  public Set<Coordinate> getUnresolvedDependencies(Module module) {
    return module
        .getDependencies()
        .stream()
        .filter(t -> !contains(t.getCoordinate()))
        .map(Dependency::getCoordinate)
        .collect(Collectors.toSet());
  }

  public void remove(Coordinate coordinate) {
    adjacencies.remove(coordinate);
  }
}
