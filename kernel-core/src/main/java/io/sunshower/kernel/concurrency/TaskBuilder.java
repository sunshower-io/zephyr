package io.sunshower.kernel.concurrency;

import io.sunshower.gyre.DirectedGraph;
import io.sunshower.gyre.Scope;
import java.util.NoSuchElementException;
import lombok.val;

@SuppressWarnings({"PMD.AvoidFieldNameMatchingMethodName", "PMD.NullAssignment"})
public class TaskBuilder {

  final String name;
  final Task task;
  private Task current;
  final ProcessBuilder processBuilder;

  public TaskBuilder(String name, Task task, ProcessBuilder processBuilder) {
    this.name = name;
    this.task = task;
    this.processBuilder = processBuilder;
    current = task;
    processBuilder.doRegister(current);
  }

  public TaskBuilder(ProcessBuilder processBuilder) {
    this.processBuilder = processBuilder;
    this.name = null;
    this.task = null;
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
    Scope context;
    if (processBuilder.context != null) {
      context = processBuilder.context;
    } else {
      context = Scope.root();
    }
    return new DefaultProcess<>(
        processBuilder.name, processBuilder.coalesce, processBuilder.parallel, context, graph);
  }

  public TaskBuilder dependsOn(Task task) {
    if (!processBuilder.tasks.containsKey(task.name)) {
      processBuilder.doRegister(task);
    }
    task(task.name);
    processBuilder.dependsOn(current, task);
    return this;
  }

  public TaskBuilder register(Task phase) {
    current = phase;
    processBuilder.doRegister(phase);
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
