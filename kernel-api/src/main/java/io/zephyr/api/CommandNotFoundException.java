package io.zephyr.api;

import io.zephyr.kernel.core.KernelException;

public class CommandNotFoundException extends KernelException {

  public CommandNotFoundException() {
    super();
  }

  public CommandNotFoundException(String message) {
    super(message);
  }

  public CommandNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public CommandNotFoundException(Throwable cause) {
    super(cause);
  }
}
