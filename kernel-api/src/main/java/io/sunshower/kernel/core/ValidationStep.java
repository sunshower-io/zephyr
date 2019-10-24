package io.sunshower.kernel.core;

@FunctionalInterface
public interface ValidationStep<T> {
  ValidationErrors validate(Validatable<T> validatable, T target);
}
