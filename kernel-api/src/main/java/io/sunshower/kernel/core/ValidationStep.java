package io.sunshower.kernel.core;

import java.util.Optional;

@FunctionalInterface
public interface ValidationStep<T> {
  Optional<ValidationError> validate(Validatable<T> validatable, T target);
}
