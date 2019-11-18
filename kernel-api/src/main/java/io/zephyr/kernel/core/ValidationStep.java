package io.zephyr.kernel.core;

import java.io.Serializable;

@FunctionalInterface
public interface ValidationStep<T> extends Serializable {
  ValidationErrors validate(Validatable<T> validatable, T target);
}
