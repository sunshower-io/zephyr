package io.sunshower.common;

import io.sunshower.kernel.core.Validatable;
import io.sunshower.kernel.core.ValidationErrors;
import io.sunshower.kernel.core.ValidationStep;
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
  public ValidationErrors validate(Validatable<T> validatable, T target) {
    ValidationErrors error = ValidationErrors.empty();
    for (var current = this; current != null; current = current.next) {

      val a = validate(current.action, validatable, target);
      if(a.isEmpty()) {
        return a;
      }
    }
    return error;
  }

  protected ValidationErrors validate(
      ValidationStep<T> action, Validatable<T> validatable, T target) {
    val result = action.validate(validatable, target);
    if (result.hasErrors()) {
      validatable.notify(result, action);
      return result;
    }
    return ValidationErrors.empty();
  }
}
