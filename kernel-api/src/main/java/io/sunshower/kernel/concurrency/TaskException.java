package io.sunshower.kernel.concurrency;

import io.sunshower.kernel.core.KernelException;

public class TaskException extends KernelException {

  public TaskException(Exception cause, TaskStatus status) {
    super(cause);
  }
}
