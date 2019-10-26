package io.sunshower.kernel.dependencies;

import io.sunshower.kernel.Coordinate;
import io.sunshower.kernel.Module;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.val;

@AllArgsConstructor
public final class DependencyNode {
  final Module module;
  final Coordinate coordinate;
  final List<DependencyNode> dependencies;

  void addDependency(DependencyNode node) {
    dependencies.add(node);
  }

  public boolean dependsOn(Coordinate coordinate) {
    for (val depNode : dependencies) {
      if (depNode.coordinate.equals(coordinate)) {
        return true;
      }
    }
    return false;
  }
}
