package io.sunshower.kernel.core;

public interface Validatable<T> {
  T getTarget();

  void validate() throws ValidationException;

  void notify(ValidationErrors error, ValidationStep<T> sourceStep);
}
