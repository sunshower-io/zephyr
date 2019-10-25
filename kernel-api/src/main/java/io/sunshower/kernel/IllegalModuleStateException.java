package io.sunshower.kernel;

import io.sunshower.kernel.core.KernelException;

public class IllegalModuleStateException extends KernelException {

  public IllegalModuleStateException() {
    super();
  }

  public IllegalModuleStateException(String message) {
    super(message);
  }

  public IllegalModuleStateException(String message, Throwable cause) {
    super(message, cause);
  }

  public IllegalModuleStateException(Throwable cause) {
    super(cause);
  }

  protected IllegalModuleStateException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
