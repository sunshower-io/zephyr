package io.sunshower.kernel.dependencies;

import io.sunshower.gyre.DirectedGraph;
import io.sunshower.gyre.Graph;
import io.sunshower.gyre.Partition;
import io.sunshower.kernel.Coordinate;
import io.sunshower.kernel.Module;
import java.util.Collection;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * this class is generally not required to be thread-safe. Consumers should protect access in
 * concurrent environments
 */
public interface DependencyGraph extends Iterable<Module> {

  Graph<DirectedGraph.Edge<Coordinate>, Coordinate> getGraph();

  Set<UnsatisfiedDependencySet> add(Module a);

  @Getter
  @AllArgsConstructor
  final class UnsatisfiedDependencySet {
    final Coordinate source;
    final Set<Coordinate> dependencies;

    public boolean isSatisfied() {
      return dependencies.isEmpty();
    }

    @Override
    public String toString() {
      return source.toCanonicalForm() + " depends on " + dependencies;
    }
  }

  /** @return the size of this graph */
  int size();

  /**
   * @param module
   * @return an unsatisified dependency set
   */
  UnsatisfiedDependencySet getUnresolvedDependencies(Module module);

  Set<UnsatisfiedDependencySet> getUnresolvedDependencies(Collection<Module> modules);

  Set<UnsatisfiedDependencySet> addAll(Collection<Module> modules);
  /**
   * @param module the module to remove from this dependency graph. This should only be called if
   *     getDependants() returns an empty set
   */
  void remove(Module module);

  /**
   * @param coordinate the coordinate to resolve
   * @return the module (if it exists).
   * @throws io.sunshower.kernel.NoSuchModuleException if the module does not exist in this graph
   */
  Module get(Coordinate coordinate);

  /**
   * @param coordinate the coordinate for which to compute dependent modules
   * @return the set of modules which have the module at <code>coordinate</code> as a dependency
   */
  Collection<Module> getDependents(Coordinate coordinate);

  /**
   * @param coordinate the coordinate to get all of the dependencies for
   * @return the set of dependencies for that coordinate
   */
  Set<Module> getDependencies(Coordinate coordinate);

  /**
   * @param coordinate the coordinate to check for membership in this graph
   * @return true if coordinate exists in this graph (i.e. if and only if add() has been called on
   *     the module with that coordinate)
   */
  boolean contains(Coordinate coordinate);

  Partition<DirectedGraph.Edge<Coordinate>, Coordinate> computeCycles();

  DependencyGraph clone();
}
