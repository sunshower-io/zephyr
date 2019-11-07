package io.sunshower.kernel.concurrency;

import io.sunshower.gyre.DirectedGraph;
import lombok.val;

import java.util.NoSuchElementException;

@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public class TaskBuilder {

  final String name;
  final Task task;
  final ProcessBuilder processBuilder;
  private NamedTask current;

  public TaskBuilder(String name, Task task, ProcessBuilder processBuilder) {
    this.name = name;
    this.task = task;
    this.processBuilder = processBuilder;
    current = new NamedTask(name, task);
    processBuilder.register(current);
  }

  public Process<String> create() {
    val graph = new TaskGraph<String>();

    for (val task : processBuilder.tasks.values()) {
      graph.add(task);

      val deps = processBuilder.dependencies.get(task.name);
      if (deps != null) {
        for (val dependency : deps) {
          graph.connect(
              task,
              dependency,
              DirectedGraph.incoming(String.format("%s dependsOn %s", task, dependency)));
        }
      }
    }
    Context context;
    if (processBuilder.context != null) {
      context = processBuilder.context;
    } else {
      context = ReductionScope.newContext();
    }
    return new DefaultProcess<>(
        processBuilder.name, processBuilder.coalesce, processBuilder.parallel, context, graph);
  }

  public TaskBuilder dependsOn(String name, Task task) {
    if (current == null) {
      throw new IllegalStateException("Error: must have a current task");
    }

    final NamedTask namedTask;
    if (task instanceof NamedTask) {
      namedTask = (NamedTask) task;
    } else {
      namedTask = new NamedTask(name, task);
    }

    processBuilder.dependsOn(current, namedTask);
    return this;
  }

  public TaskBuilder register(String name, Task phase) {
    NamedTask c;
    if (phase instanceof NamedTask) {
      c = (NamedTask) phase;
    } else {
      c = new NamedTask(name, phase);
    }
    current = c;
    processBuilder.register(c);
    return this;
  }

  public TaskBuilder task(String s) {
    current = processBuilder.tasks.get(s);
    if (current == null) {
      throw new NoSuchElementException("No task with name " + s);
    }
    return this;
  }

  public TaskBuilder dependsOn(String... dependencies) {
    if (current == null) {
      throw new IllegalStateException("Error: no current task.  Call task() or register() first");
    }
    for (String s : dependencies) {
      processBuilder.dependsOn(current, processBuilder.tasks.get(s));
    }
    return this;
  }
}
