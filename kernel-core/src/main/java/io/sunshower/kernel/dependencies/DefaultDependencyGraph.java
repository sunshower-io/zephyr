package io.sunshower.kernel.dependencies;

import io.sunshower.gyre.*;
import io.sunshower.kernel.Coordinate;
import io.sunshower.kernel.Module;
import java.util.*;
import lombok.NonNull;
import lombok.val;

@SuppressWarnings({
  "PMD.DataflowAnomalyAnalysis",
  "PMD.AvoidInstantiatingObjectsInLoops",
  "PMD.UnusedPrivateMethod"
})
public final class DefaultDependencyGraph implements DependencyGraph, Cloneable {

  final Map<Coordinate, Module> modules;
  final Graph<DirectedGraph.Edge<Coordinate>, Coordinate> dependencyGraph;

  public DefaultDependencyGraph() {
    modules = new HashMap<>();
    dependencyGraph = new AbstractDirectedGraph<>();
  }

  private DefaultDependencyGraph(DefaultDependencyGraph graph) {
    modules = new HashMap<>(graph.modules);
    dependencyGraph = graph.dependencyGraph.clone();
  }

  @Override
  public Graph<DirectedGraph.Edge<Coordinate>, Coordinate> getGraph() {
    return dependencyGraph;
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

    val result = new HashSet<Coordinate>();
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
    val results = new HashSet<UnsatisfiedDependencySet>();
    for (val module : modules) {
      val unsatisfied = new HashSet<Coordinate>();
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
    val results = new HashSet<UnsatisfiedDependencySet>();
    for (val module : modules) {
      val unsatisfied = new HashSet<Coordinate>();
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
    val results = new HashSet<Module>();
    for (val dependent : dependents) {
      results.add(modules.get(dependencyGraph.getSource(dependent)));
    }
    return results;
  }

  @Override
  public Set<Module> getDependencies(Coordinate coordinate) {
    val neighbors = dependencyGraph.neighbors(coordinate);
    val result = new HashSet<Module>(neighbors.size());
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
