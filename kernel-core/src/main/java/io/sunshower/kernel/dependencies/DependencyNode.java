package io.sunshower.kernel.dependencies;

import io.sunshower.kernel.Coordinate;
import io.sunshower.kernel.Module;
import java.util.List;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public final class DependencyNode {
  final Module module;
  final Coordinate coordinate;
  final List<DependencyNode> dependencies;

  void addDependency(DependencyNode node) {
    dependencies.add(node);
  }
}
