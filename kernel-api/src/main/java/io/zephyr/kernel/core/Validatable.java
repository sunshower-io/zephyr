package io.zephyr.kernel.core;

import java.io.Serializable;

public interface Validatable<T> extends Serializable {
  T getTarget();

  void validate() throws ValidationException;

  void notify(ValidationErrors error, ValidationStep<T> sourceStep);
}
