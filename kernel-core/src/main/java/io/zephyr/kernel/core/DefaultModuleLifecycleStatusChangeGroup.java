package io.zephyr.kernel.core;

import io.sunshower.gyre.*;
import io.zephyr.kernel.Coordinate;
import io.zephyr.kernel.concurrency.*;
import io.zephyr.kernel.concurrency.Process;
import io.zephyr.kernel.concurrency.Task;
import io.zephyr.kernel.core.actions.plugin.PluginStartTask;
import io.zephyr.kernel.core.actions.plugin.PluginStopTask;
import io.zephyr.kernel.module.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import lombok.val;

@SuppressWarnings({
  "PMD.UnusedPrivateMethod",
  "PMD.DataflowAnomalyAnalysis",
  "PMD.AvoidInstantiatingObjectsInLoops"
})
final class DefaultModuleLifecycleStatusChangeGroup implements ModuleLifecycleStatusGroup {

  private final Kernel kernel;
  private final ModuleManager moduleManager;
  private final Process<String> process;
  private final ModuleLifecycleChangeGroup request;

  /**
   * precondition: every task has the same type (start, stop, restart, etc.)
   *
   * @param request
   */
  public DefaultModuleLifecycleStatusChangeGroup(
      final Kernel kernel,
      final ModuleManager moduleManager,
      final ModuleLifecycleChangeGroup request) {
    this.kernel = kernel;
    this.request = request;
    this.moduleManager = moduleManager;
    this.process = createProcess(request);
  }

  private Process<String> createProcess(ModuleLifecycleChangeGroup request) {

    //    val tasks = Tasks.newProcess("module:lifecycle:change").coalesce().parallel().task();
    //    val
    val taskGraph = new TaskGraph<String>();
    val tasks = new HashMap<Coordinate, Task>();

    for (val task : request.getRequests()) {
      val actions = task.getLifecycleActions();
      if (actions == ModuleLifecycle.Actions.Stop) {
        addStopAction(task, taskGraph, tasks);
      } else if (actions == ModuleLifecycle.Actions.Activate) {
        addStartAction(task, taskGraph, tasks);
      }
    }
    return new DefaultProcess<>("module:lifecycle:change", false, true, Scope.root(), taskGraph);
  }

  @Override
  public CompletionStage<Process<String>> commit() {
    return kernel.getScheduler().submit(process);
  }

  @Override
  public Process<String> getProcess() {
    return process;
  }

  @Override
  public Set<? extends ModuleRequest> getRequests() {
    return new HashSet<>(request.getRequests());
  }

  private void addStopAction(
      ModuleLifecycleChangeRequest task, TaskGraph<String> tasks, Map<Coordinate, Task> existing) {
    val reachability =
        new ReverseSubgraphTransformation<DirectedGraph.Edge<Coordinate>, Coordinate>(
                task.getCoordinate())
            .apply(moduleManager.getDependencyGraph().getGraph());
    val schedule =
        new ParallelScheduler<DirectedGraph.Edge<Coordinate>, Coordinate>()
            .apply(reachability)
            .getTasks();

    Task source;
    if (!existing.containsKey(task.getCoordinate())) {
      source = new PluginStopTask(task.getCoordinate(), moduleManager, kernel);
      tasks.add(source);
      existing.put(task.getCoordinate(), source);
    } else {
      source = existing.get(task.getCoordinate());
    }

    for (val group : schedule) {
      for (val taskSet : group.getTasks()) {
        final Task actualTask;
        if (!existing.containsKey(taskSet.getValue())) {
          actualTask = new PluginStopTask(taskSet.getValue(), moduleManager, kernel);
          tasks.add(actualTask);
          existing.put(taskSet.getValue(), actualTask);
        } else {
          actualTask = existing.get(taskSet.getValue());
        }

        if (!(taskSet.getValue().equals(task.getCoordinate())
            || tasks.containsEdge(source, actualTask))) {
          tasks.connect(source, actualTask, DirectedGraph.incoming("depends-on"));
        }
      }
    }
  }

  private void addStartAction(
      ModuleLifecycleChangeRequest task, TaskGraph<String> tasks, Map<Coordinate, Task> existing) {

    val reachability =
        new SubgraphTransformation<DirectedGraph.Edge<Coordinate>, Coordinate>(task.getCoordinate())
            .apply(moduleManager.getDependencyGraph().getGraph());
    val schedule =
        new ParallelScheduler<DirectedGraph.Edge<Coordinate>, Coordinate>()
            .apply(reachability)
            .getTasks();

    Task source;
    if (!existing.containsKey(task.getCoordinate())) {
      source = new PluginStartTask(task.getCoordinate(), moduleManager, kernel);
      tasks.add(source);
      existing.put(task.getCoordinate(), source);
    } else {
      source = existing.get(task.getCoordinate());
    }

    for (val group : schedule) {
      for (val taskSet : group.getTasks()) {
        final Task actualTask;
        if (!existing.containsKey(taskSet.getValue())) {
          actualTask = new PluginStartTask(taskSet.getValue(), moduleManager, kernel);
          tasks.add(actualTask);
          existing.put(taskSet.getValue(), actualTask);
        } else {
          actualTask = existing.get(taskSet.getValue());
        }

        if (!(taskSet.getValue().equals(task.getCoordinate())
            || tasks.containsEdge(source, actualTask))) {
          tasks.connect(source, actualTask, DirectedGraph.incoming("depends-on"));
        }
      }
    }
  }
}
