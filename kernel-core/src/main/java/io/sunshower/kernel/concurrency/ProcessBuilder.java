package io.sunshower.kernel.concurrency;

import io.sunshower.gyre.Scope;
import io.sunshower.kernel.misc.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public class ProcessBuilder {

  /** Mutable state */
  boolean parallel;

  boolean coalesce;
  Scope context;

  /** Immutable state */
  final String name;

  final Map<String, Task> tasks;
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

  public ProcessBuilder withContext(Scope ctx) {
    this.context = ctx;
    return this;
  }

  @SuppressFBWarnings
  public TaskBuilder register(Task task) {
    return new TaskBuilder(task.name, task, this);
  }

  ProcessBuilder doRegister(Task task) {

    tasks.put(task.name, task);
    return this;
  }

  ProcessBuilder dependsOn(Task source, Task target) {
    if (target == null) {
      throw new IllegalStateException("A task can't depend on a null dependency");
    }
    dependencies.computeIfAbsent(source.name, c -> new ArrayList<>()).add(target);
    return this;
  }

  public TaskBuilder task() {
    return new TaskBuilder(this);
  }
}
