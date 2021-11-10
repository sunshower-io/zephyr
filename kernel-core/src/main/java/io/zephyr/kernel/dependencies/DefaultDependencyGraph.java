package io.zephyr.kernel.dependencies;

import io.sunshower.gyre.*;
import io.zephyr.kernel.Coordinate;
import io.zephyr.kernel.Module;
import java.util.*;
import lombok.NonNull;
import lombok.val;

@SuppressWarnings({
  "PMD.DataflowAnomalyAnalysis",
  "PMD.AvoidInstantiatingObjectsInLoops",
  "PMD.UnusedPrivateMethod"
})
public final class DefaultDependencyGraph implements DependencyGraph, Cloneable {

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
  public Set<UnsatisfiedDependencySet> getUnresolvedDependencies(Collection<Module> modules) {
    val prospective = new HashMap<>(this.modules);
    for (val module : modules) {
      prospective.put(module.getCoordinate(), module);
    }
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
    val prospective = new HashMap<>(this.modules);
    for (val module : modules) {
      prospective.put(module.getCoordinate(), module);
    }
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
}
