package io.sunshower.kernel.status;

import io.sunshower.kernel.core.KernelException;
import lombok.Getter;

public class StatusException extends KernelException {
  @Getter private Status status;

  public StatusException(Status status) {
    super(status.message);
    this.status = status;
  }
}
