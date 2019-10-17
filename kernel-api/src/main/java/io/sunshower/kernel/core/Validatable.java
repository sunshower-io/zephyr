package io.sunshower.kernel.core;

public interface Validatable<T> {
  T getTarget();

  void notify(ValidationError error);

  void validate() throws ValidationException;
}
