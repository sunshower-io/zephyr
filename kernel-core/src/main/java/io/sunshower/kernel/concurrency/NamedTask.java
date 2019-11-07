package io.sunshower.kernel.concurrency;

import lombok.ToString;

@ToString
public class NamedTask implements Task {

  final Task task;
  final String name;

  public NamedTask(String name, Task task) {
    this.name = name;
    this.task = task;
  }

  @Override
  public TaskValue run(Context context) {
    return task.run(context);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof NamedTask)) return false;

    NamedTask namedTask = (NamedTask) o;

    return name.equals(namedTask.name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }
}
