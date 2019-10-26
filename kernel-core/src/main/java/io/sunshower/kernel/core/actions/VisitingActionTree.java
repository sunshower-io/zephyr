package io.sunshower.kernel.core.actions;

import io.sunshower.kernel.Coordinate;
import io.sunshower.kernel.Module;
import io.sunshower.kernel.core.ActionTree;
import io.sunshower.kernel.dependencies.DependencyGraph;
import java.util.ArrayList;
import java.util.List;
import lombok.val;

@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class VisitingActionTree implements ActionTree {
  final ActionNode root;
  final DependencyGraph graph;

  VisitingActionTree(ActionNode root, DependencyGraph graph) {
    this.root = root;
    this.graph = graph;
  }

  public static ActionTree createFrom(Module module, DependencyGraph graph) {
    val root = new ActionNode(module);
    build(root, module, graph);
    return new VisitingActionTree(root, graph);
  }

  @SuppressWarnings({"PMD.AvoidInstantiatingObjectsInLoops", "PMD.UnusedPrivateMethod"})
  private static void build(ActionNode current, Module module, DependencyGraph graph) {
    val dependencies = module.getDependencies();
    for (val dependency : dependencies) {
      val depMod = graph.get(dependency.getCoordinate());
      val depNode = new ActionNode(depMod);
      current.addDependency(depNode);
      build(depNode, depMod, graph);
    }
  }

  @Override
  public int size() {
    return sizeOf(root, 0);
  }

  @Override
  public int height() {
    return heightOf(root, 0);
  }

  @Override
  public List<Coordinate> getLevel(int level) {
    return null;
  }

  static final class ActionNode {
    final Module module;
    final List<ActionNode> dependencies;

    ActionNode(Module module) {
      this.module = module;
      this.dependencies = new ArrayList<>();
    }

    void addDependency(ActionNode dependency) {
      dependencies.add(dependency);
    }
  }

  static int sizeOf(ActionNode node, int current) {
    int next = current + 1;
    for (ActionNode child : node.dependencies) {
      next = sizeOf(child, next);
    }
    return next;
  }

  static int heightOf(ActionNode root, int current) {
    if (root.dependencies.isEmpty()) {
      return 1;
    }

    int max = 0;

    for (ActionNode node : root.dependencies) {
      val h = heightOf(node, current);
      if (h > max) {
        max = h;
      }
    }
    return 1 + max;
  }
}
