package io.sunshower.kernel;

import io.sunshower.kernel.core.KernelException;

public class ModuleException extends KernelException {
  public ModuleException() {}

  public ModuleException(String message) {
    super(message);
  }

  public ModuleException(String message, Throwable cause) {
    super(message, cause);
  }

  public ModuleException(Throwable cause) {
    super(cause);
  }

  public ModuleException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
