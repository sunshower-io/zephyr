package io.sunshower.kernel.core;

import io.sunshower.gyre.DirectedGraph;
import io.sunshower.gyre.ParallelScheduler;
import io.sunshower.gyre.ReverseSubgraphTransformation;
import io.sunshower.gyre.SubgraphTransformation;
import io.sunshower.kernel.Coordinate;
import io.sunshower.kernel.concurrency.Process;
import io.sunshower.kernel.concurrency.TaskBuilder;
import io.sunshower.kernel.concurrency.Tasks;
import io.sunshower.kernel.core.actions.plugin.PluginStopTask;
import io.sunshower.kernel.module.ModuleLifecycleChangeGroup;
import io.sunshower.kernel.module.ModuleLifecycleChangeRequest;
import io.sunshower.kernel.module.ModuleLifecycleStatusGroup;
import io.sunshower.kernel.module.ModuleRequest;
import lombok.val;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletionStage;

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

    val tasks = Tasks.newProcess("module:lifecycle:change").coalesce().parallel().task();

    for (val task : request.getRequests()) {
      val actions = task.getLifecycleActions();
      switch (actions) {
        case Stop:
          {
            addStopAction(task, tasks);
            continue;
          }
        case Activate:
          addStartAction(task, tasks);
      }
    }
    return tasks.create();
  }

  private void addStopAction(ModuleLifecycleChangeRequest task, TaskBuilder tasks) {
    val reachability =
        new ReverseSubgraphTransformation<DirectedGraph.Edge<Coordinate>, Coordinate>(
                task.getCoordinate())
            .apply(moduleManager.getDependencyGraph().getGraph());
    val schedule =
        new ParallelScheduler<DirectedGraph.Edge<Coordinate>, Coordinate>()
            .apply(reachability)
            .getTasks();

    var pjp = JoinPoint.newJoinPoint();
    for (int i = 0; i < schedule.size(); i++) {
      tasks.register(pjp);
      val taskSet = schedule.get(i);
      for (val el : taskSet.getTasks()) {
        val actualTask = new PluginStopTask(el.getValue(), moduleManager, kernel);
        tasks.register(actualTask);
        tasks.task(pjp.getName()).dependsOn(actualTask.getName());
      }
      pjp = JoinPoint.newJoinPoint();
    }
  }

  private void addStartAction(ModuleLifecycleChangeRequest task, TaskBuilder tasks) {}

  public CompletionStage<String> commit() {
    return kernel.getScheduler().submit(process).thenApply(t -> "started");
  }

  public Process<String> getProcess() {
    return process;
  }

  @Override
  public Set<? extends ModuleRequest> getRequests() {
    return new HashSet<>(request.getRequests());
  }
}
