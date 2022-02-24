package io.zephyr.kernel.concurrency;

import java.util.Optional;

public class TaskPhaseEvent {

  private final Task task;
  private final TaskException exception;

  public TaskPhaseEvent(Task task) {
    this.task = task;
    this.exception = null;
  }

  public TaskPhaseEvent(Task taskDef, TaskException ex) {
    this.task = taskDef;
    this.exception = ex;
  }

  public Optional<Exception> getError() {
    return Optional.ofNullable(exception);
  }

  public Task getTask() {
    return task;
  }
}
