package io.zephyr.kernel.dependencies;

import io.sunshower.gyre.AbstractDirectedGraph;
import io.sunshower.gyre.CompactTrieMap;
import io.sunshower.gyre.DirectedGraph;
import io.sunshower.gyre.EdgeFilters;
import io.sunshower.gyre.Graph;
import io.sunshower.gyre.GraphWriter;
import io.sunshower.gyre.Partition;
import io.sunshower.gyre.StronglyConnectedComponents;
import io.sunshower.gyre.TrieMap;
import io.zephyr.kernel.Coordinate;
import io.zephyr.kernel.Dependency;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.core.ModuleCoordinate;
import io.zephyr.kernel.log.Logging;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.val;

@SuppressWarnings({
  "PMD.DataflowAnomalyAnalysis",
  "PMD.AvoidInstantiatingObjectsInLoops",
  "PMD.UnusedPrivateMethod"
})
public final class DefaultDependencyGraph implements DependencyGraph, Cloneable {

  static final Logger log = Logging.get(DependencyGraph.class);
  final TrieMap<Coordinate, Module> modules;
  final Graph<DirectedGraph.Edge<Coordinate>, Coordinate> dependencyGraph;

  public DefaultDependencyGraph() {
    dependencyGraph = new AbstractDirectedGraph<>();
    modules = new CompactTrieMap<>(new CoordinateAnalyzer());
  }

  private DefaultDependencyGraph(DefaultDependencyGraph graph) {
    dependencyGraph = graph.dependencyGraph.clone();
    modules = new CompactTrieMap<>(new CoordinateAnalyzer(), graph.modules);
  }

  @Override
  public Graph<DirectedGraph.Edge<Coordinate>, Coordinate> getGraph() {
    return dependencyGraph;
  }

  @Override
  public Module latest(Coordinate coordinate) {
    return firstOfLevel(coordinate, Comparator.reverseOrder());
  }

  @Override
  public Module earliest(Coordinate coordinate) {
    return firstOfLevel(coordinate, Module::compareTo);
  }

  @Override
  public Module firstOfLevel(Coordinate coordinate, Comparator<Module> comparator) {
    val level = getModules(coordinate);
    if (level.isEmpty()) {
      return null;
    }

    level.sort(comparator);
    return level.get(0);
  }

  @Override
  public List<Module> getModules(Coordinate coordinate) {
    return modules.level(coordinate);
  }

  @Override
  public Set<UnsatisfiedDependencySet> add(Module a) {
    return addAll(Collections.singleton(a));
  }

  @Override
  public int size() {
    return dependencyGraph.size();
  }

  @Override
  public UnsatisfiedDependencySet getUnresolvedDependencies(@NonNull Module module) {
    val coordinate = module.getCoordinate();

    val dependencies = module.getDependencies();

    val result = new LinkedHashSet<Coordinate>();
    for (val dependency : dependencies) {
      val depcoord = dependency.getCoordinate();
      if (!modules.containsKey(depcoord)) {
        result.add(depcoord);
      }
    }
    if (result.isEmpty()) {
      return new UnsatisfiedDependencySet(coordinate, Collections.emptySet());
    } else {
      return new UnsatisfiedDependencySet(coordinate, result);
    }
  }

  @Override
  public @NonNull Set<UnsatisfiedDependencySet> resolveDependencies(Collection<Module> modules) {
    val results = new HashSet<UnsatisfiedDependencySet>();
    val installationGroupModules = resolveInstallationGroupModules(modules);
    for (val module : modules) {
      resolveDependenciesFor(module, results, installationGroupModules);
    }
    return results;
  }

  @Override
  public Set<UnsatisfiedDependencySet> getUnresolvedDependencies(Collection<Module> modules) {
    val prospective = resolveInstallationGroupModules(modules);
    val results = new LinkedHashSet<UnsatisfiedDependencySet>();
    for (val module : modules) {
      val unsatisfied = new LinkedHashSet<Coordinate>();
      for (val dependency : module.getDependencies()) {
        val depcoord = dependency.getCoordinate();
        if (!prospective.containsKey(depcoord)) {
          unsatisfied.add(depcoord);
        }
      }

      if (unsatisfied.isEmpty()) {
        results.add(new UnsatisfiedDependencySet(module.getCoordinate(), Collections.emptySet()));
      } else {
        results.add(new UnsatisfiedDependencySet(module.getCoordinate(), unsatisfied));
      }
    }
    return results;
  }

  @Override
  public Set<UnsatisfiedDependencySet> addAll(Collection<Module> modules) {
    val prospective = resolveInstallationGroupModules(modules);
    val results = new LinkedHashSet<UnsatisfiedDependencySet>();
    for (val module : modules) {
      val unsatisfied = new LinkedHashSet<Coordinate>();
      for (val dependency : module.getDependencies()) {
        val depcoord = dependency.getCoordinate();
        if (!prospective.containsKey(depcoord)) {
          unsatisfied.add(depcoord);
        }
      }
      if (unsatisfied.isEmpty()) {
        results.add(new UnsatisfiedDependencySet(module.getCoordinate(), Collections.emptySet()));
        val coordinate = module.getCoordinate();
        this.modules.put(coordinate, module);
        dependencyGraph.add(coordinate);
        for (val dep : module.getDependencies()) {
          dependencyGraph.connect(
              coordinate, dep.getCoordinate(), DirectedGraph.outgoing(dep.getCoordinate()));
        }
      } else {
        results.add(new UnsatisfiedDependencySet(module.getCoordinate(), unsatisfied));
      }
    }
    return results;
  }

  @Override
  public void remove(Module module) {
    val coord = module.getCoordinate();
    dependencyGraph.remove(coord);
    modules.remove(coord);
  }

  @Override
  public Module get(Coordinate coordinate) {
    return modules.get(coordinate);
  }

  @Override
  public Collection<Module> getDependents(Coordinate coordinate) {
    val module = modules.get(coordinate);
    if (module == null) {
      return Collections.emptySet();
    }
    val dependents = dependencyGraph.getDependents(coordinate, EdgeFilters.acceptAll());
    val results = new LinkedHashSet<Module>();
    for (val dependent : dependents) {
      results.add(modules.get(dependencyGraph.getSource(dependent)));
    }
    return results;
  }

  @Override
  public Set<Module> getDependencies(Coordinate coordinate) {
    val neighbors = dependencyGraph.neighbors(coordinate);
    val result = new LinkedHashSet<Module>(neighbors.size());
    for (val neighbor : neighbors) {
      result.add(modules.get(neighbor));
    }
    return result;
  }

  @Override
  public boolean contains(Coordinate coordinate) {
    return modules.containsKey(coordinate);
  }

  @Override
  public Partition<DirectedGraph.Edge<Coordinate>, Coordinate> computeCycles() {
    return new StronglyConnectedComponents<DirectedGraph.Edge<Coordinate>, Coordinate>()
        .apply(dependencyGraph);
  }

  @Override
  @SuppressWarnings({"PMD.ProperCloneImplementation", "CloneMethodReturnTypeMustMatchClassName"})
  public DefaultDependencyGraph clone() {
    return new DefaultDependencyGraph(this);
  }

  @Override
  public Iterator<Module> iterator() {
    return modules.values().iterator();
  }

  @Override
  public String toString() {
    return new GraphWriter<DirectedGraph.Edge<Coordinate>, Coordinate>().write(dependencyGraph);
  }

  private Map<Coordinate, Module> resolveInstallationGroupModules(Collection<Module> modules) {
    val prospective = new HashMap<>(this.modules);
    for (val module : modules) {
      prospective.put(module.getCoordinate(), module);
    }
    return prospective;
  }

  private void resolveDependenciesFor(
      Module module,
      Set<UnsatisfiedDependencySet> results,
      Map<Coordinate, Module> installationGroupModules) {
    val unsatisfied = new HashSet<Coordinate>();
    val unsatisifiedDependencySet =
        new UnsatisfiedDependencySet(module.getCoordinate(), unsatisfied);
    for (val dependency : module.getDependencies()) {
      resolveDependency(module, dependency, unsatisfied, installationGroupModules);
      if (!unsatisfied.isEmpty()) {
        results.add(unsatisifiedDependencySet);
      }
    }
  }

  private void resolveDependency(
      Module module,
      Dependency dependency,
      Set<Coordinate> results,
      Map<Coordinate, Module> installationGroupModules) {
    val matching = collectMatchingFrom(dependency, installationGroupModules);
    if (matching.isEmpty()) {
      results.add(new UnvalidatedCoordinate(dependency.getCoordinateSpecification()));
    } else {
      Collections.sort(matching);
      val fst = matching.get(matching.size() - 1);
      if (log.isLoggable(Level.INFO)) {
        log.log(
            Level.INFO,
            "dependency.graph.selected.module.prelude",
            new Object[] {dependency, module.getCoordinate().toCanonicalForm()});
        for (val dep : matching) {
          log.log(Level.INFO, "dependency.graph.selected.module.node", dep.getCoordinate());
        }
      }
      dependency.setCoordinate(fst.getCoordinate());
    }
  }

  /**
   * process:
   *
   * <p>1. Search through existing modules. Add to list 2. Search through installation group
   * modules, add to list 3. If not empty, sort coordinate ascending 4. Pick first as resolution 5.
   * If empty, add unresolved dependency to dependency set
   *
   * @param dependency the dependency to resolve
   * @param installationGroupModules the current installation group
   * @return a list of matching dependencies ordered version ascending
   */
  private List<Module> collectMatchingFrom(
      Dependency dependency, Map<Coordinate, Module> installationGroupModules) {

    val spec = dependency.getCoordinateSpecification();
    val query = ModuleCoordinate.group(spec.getGroup()).name(spec.getName());
    val existing =
        getModules(query).stream()
            .filter(f -> f.getCoordinate().satisfies(spec.getVersionSpecification()))
            .collect(Collectors.toList());

    for (val module : installationGroupModules.values()) {
      val coordinate = module.getCoordinate();
      if (Objects.equals(coordinate.getGroup(), spec.getGroup())
          && Objects.equals(coordinate.getName(), spec.getName())
          && coordinate.satisfies(spec.getVersionSpecification())) {
        existing.add(module);
      }
    }
    return existing;
  }
}
