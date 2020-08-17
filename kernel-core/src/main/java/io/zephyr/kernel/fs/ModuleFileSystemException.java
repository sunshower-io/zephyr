package io.zephyr.kernel.fs;

import io.zephyr.kernel.core.KernelException;

public class ModuleFileSystemException extends KernelException {

  static final long serialVersionUID = 6739239491835362085L;

  public ModuleFileSystemException() {
    super();
  }

  public ModuleFileSystemException(String message) {
    super(message);
  }

  public ModuleFileSystemException(String message, Throwable cause) {
    super(message, cause);
  }

  public ModuleFileSystemException(Throwable cause) {
    super(cause);
  }

  protected ModuleFileSystemException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
