package io.sunshower.kernel.concurrency;

import io.sunshower.kernel.core.KernelException;
import lombok.Getter;

public class TaskException extends KernelException {

  @Getter final TaskStatus status;

  public TaskException(String message, Exception cause, TaskStatus status) {
    super(message, cause);
    this.status = status;
  }

  public TaskException(Exception cause, TaskStatus status) {
    super(cause);
    this.status = status;
  }

  public TaskException(TaskStatus unrecoverable) {
    this.status = unrecoverable;
  }
}
