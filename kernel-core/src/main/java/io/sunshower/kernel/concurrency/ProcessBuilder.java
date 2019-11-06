package io.sunshower.kernel.concurrency;

import java.util.HashMap;
import java.util.Map;

public class ProcessBuilder {
  final String name;

  final Map<String, NamedTask> tasks;
  /** mutable state */
  Context context;

  public ProcessBuilder(String name) {
    this.name = name;
    this.tasks = new HashMap<>();
  }

  public ProcessBuilder withContext(Context ctx) {
    this.context = ctx;
    return this;
  }

  public TaskBuilder register(String name, Task task) {
    return new TaskBuilder(name, task, this);
  }

  ProcessBuilder register(NamedTask task) {
    tasks.put(task.name, task);
    return this;
  }
}
