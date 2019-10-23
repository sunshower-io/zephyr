package io.sunshower.kernel.core;

public class ContextValueNotFoundException extends KernelException {

  private final String key;

  public ContextValueNotFoundException(String msg, String key) {
    super(msg);
    this.key = key;
  }

  public ContextValueNotFoundException(String key) {
    this("Context value was not found " + key, key);
  }
}
