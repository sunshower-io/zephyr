package io.zephyr.platform.api;

import io.zephyr.kernel.core.KernelException;
import java.util.EnumSet;

public class PlatformException extends KernelException {

  public PlatformException() {
    this(getErrorMessage());
  }

  public PlatformException(String message) {
    super(message);
  }

  public PlatformException(String message, Throwable cause) {
    super(message, cause);
  }

  public PlatformException(Throwable cause) {
    super(cause);
  }

  protected PlatformException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  static String getErrorMessage() {
    return String.format(
        "Unknown operating system '%s' is not one of: [%s]",
        Platform.CURRENT, EnumSet.allOf(Platform.OperatingSystem.class));
  }
}
