package io.zephyr.kernel;

import io.zephyr.kernel.core.KernelException;
import java.util.List;
import lombok.val;

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

  public ModuleException(String message, List<Exception> exceptions) {
    this(message);
    for (val exception : exceptions) {
      addSuppressed(exception);
    }
  }
}
