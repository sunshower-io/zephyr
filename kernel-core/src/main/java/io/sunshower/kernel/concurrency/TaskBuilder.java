package io.sunshower.kernel.concurrency;

import lombok.val;

public class TaskBuilder {

  final String name;
  final Task task;
  final ProcessBuilder processBuilder;

  public TaskBuilder(String name, Task task, ProcessBuilder processBuilder) {
    this.name = name;
    this.task = task;
    this.processBuilder = processBuilder;
    processBuilder.register(new NamedTask(name, task));
  }

  public Process<String> create() {
    val graph = new TaskGraph<String>();

    for (val task : processBuilder.tasks.values()) {
      graph.add(task);
    }
    //    return new DefaultProcess<>(graph);
    return null;
  }
}
