package io.sunshower.kernel;

import lombok.Getter;

public class KernelExtensionException extends KernelException {

  @Getter private KernelExtensionDescriptor source;

  public KernelExtensionException(KernelExtensionDescriptor source) {
    this.source = source;
  }

  public KernelExtensionException(String message, KernelExtensionDescriptor source) {
    super(message);
    this.source = source;
  }

  public KernelExtensionException(
      String message, Throwable cause, KernelExtensionDescriptor source) {
    super(message, cause);
    this.source = source;
  }

  public KernelExtensionException(Throwable cause, KernelExtensionDescriptor source) {
    super(cause);
    this.source = source;
  }

  public KernelExtensionException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
