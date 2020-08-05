package io.zephyr.bundle.sfx;

public interface Validatable {

  /** validate this object and throw descriptive IllegalArgumentExceptions upon failures */
  void validate() throws IllegalArgumentException;
}
