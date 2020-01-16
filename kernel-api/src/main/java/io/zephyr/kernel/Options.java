package io.zephyr.kernel;

import io.zephyr.kernel.core.Validatable;
import io.zephyr.kernel.core.ValidationErrors;
import io.zephyr.kernel.core.ValidationException;
import io.zephyr.kernel.core.ValidationStep;
import java.io.Serializable;

public interface Options<T extends Options<T>> extends Serializable, Validatable<T> {
  Options<?> EMPTY = new EmptyOptions<>();
}

final class EmptyOptions<T> implements Options<EmptyOptions<T>> {
  @Override
  public EmptyOptions<T> getTarget() {
    return this;
  }

  @Override
  public void validate() throws ValidationException {}

  @Override
  public void notify(ValidationErrors error, ValidationStep<EmptyOptions<T>> sourceStep) {}
}
