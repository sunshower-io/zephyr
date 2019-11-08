package io.sunshower.kernel.concurrency;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public class ProcessBuilder {

  /** Mutable state */
  boolean parallel;

  boolean coalesce;
  Context context;

  /** Immutable state */
  final String name;

  final Map<String, NamedTask> tasks;
  final Map<String, List<Task>> dependencies;
  /** mutable state */
  public ProcessBuilder(String name) {
    this.name = name;
    this.tasks = new HashMap<>();
    this.dependencies = new HashMap<>();
  }

  public ProcessBuilder coalesce() {
    this.coalesce = true;
    return this;
  }

  public ProcessBuilder parallel() {
    this.parallel = true;
    return this;
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

  ProcessBuilder dependsOn(NamedTask source, NamedTask target) {
    if (target == null) {
      throw new IllegalStateException("A task can't depend on a null dependency");
    }
    dependencies.computeIfAbsent(source.name, c -> new ArrayList<>()).add(target);
    return this;
  }
}
