package io.sunshower.common;

import io.sunshower.kernel.core.Validatable;
import io.sunshower.kernel.core.ValidationError;
import io.sunshower.kernel.core.ValidationStep;
import java.util.Optional;
import lombok.val;

public class ChainedValidationStep<T> implements ValidationStep<T> {
  private ChainedValidationStep<T> last;
  private ChainedValidationStep<T> next;
  private final ValidationStep<T> action;

  public ChainedValidationStep(ValidationStep<T> action) {
    this.action = action;
  }

  public ChainedValidationStep<T> add(ValidationStep<T> n) {
    val nval = new ChainedValidationStep<>(n);
    if (next == null) {
      next = nval;
      last = next;
    } else {
      last.next = nval;
      last = nval;
    }
    return last;
  }

  @Override
  public Optional<ValidationError> validate(Validatable<T> validatable, T target) {
    Optional<ValidationError> error = null;
    for (var current = this; current != null; current = current.next) {
      error = validate(current.action, validatable, target);
    }
    return error;
  }

  protected Optional<ValidationError> validate(
      ValidationStep<T> action, Validatable<T> validatable, T target) {
    val result = action.validate(validatable, target);
    if (result.isPresent()) {
      validatable.notify(result.get());
      return result;
    }
    return Optional.empty();
  }
}
