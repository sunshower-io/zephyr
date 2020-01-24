package io.zephyr.kernel;

import io.zephyr.kernel.core.KernelException;

public class ObjectCheckException extends KernelException {

  final Object object;

  public ObjectCheckException(Object object, String message) {
    super(message);
    this.object = object;
  }
}
