package io.zephyr.kernel.status;

import io.zephyr.kernel.core.KernelException;
import lombok.Getter;

public class StatusException extends KernelException {
  @Getter private Status status;

  public StatusException(Status status) {
    super(status.message);
    this.status = status;
  }
}
