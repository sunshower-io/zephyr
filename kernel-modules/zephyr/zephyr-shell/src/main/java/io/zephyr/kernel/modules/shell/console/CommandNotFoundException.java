package io.zephyr.kernel.modules.shell.console;

import io.zephyr.kernel.core.KernelException;

/** todo: move CLI to kernel module (should not be in API) */
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
