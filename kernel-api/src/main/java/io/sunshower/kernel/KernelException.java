package io.sunshower.kernel;

public class KernelException extends RuntimeException {

  public KernelException() {}

  public KernelException(String message) {
    super(message);
  }

  public KernelException(String message, Throwable cause) {
    super(message, cause);
  }

  public KernelException(Throwable cause) {
    super(cause);
  }

  public KernelException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
