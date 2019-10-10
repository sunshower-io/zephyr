package io.sunshower.kernel;

public class KernelExtensionConflictException extends KernelException {
  private KernelExtensionDescriptor descriptor;

  public KernelExtensionConflictException() {
    super();
  }

  public KernelExtensionConflictException(String message, KernelExtensionDescriptor descriptor) {
    super(message);
    this.descriptor = descriptor;
  }

  public KernelExtensionConflictException(String message, Throwable cause) {
    super(message, cause);
  }

  public KernelExtensionConflictException(Throwable cause) {
    super(cause);
  }

  public KernelExtensionConflictException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
