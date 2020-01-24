package io.zephyr.kernel.core;

import java.util.ArrayList;
import java.util.List;
import lombok.val;

public class AbstractValidatable<T extends Validatable<T>> implements Validatable<T> {

  private final T target;
  private final List<ValidationStep<T>> steps;
  private List<ValidationError> validationErrors;

  @SuppressWarnings("unchecked")
  public AbstractValidatable(T target) {
    steps = new ArrayList<>();
    this.target = target == null ? (T) this : target;
  }

  protected AbstractValidatable() {
    this(null);
  }

  protected void registerStep(ValidationStep<T> step) {
    this.steps.add(step);
  }

  @Override
  public T getTarget() {
    return target;
  }

  @Override
  public void validate() throws ValidationException {
    for (val step : steps) {
      step.validate(this, target);
    }
  }

  @Override
  public void notify(ValidationErrors error, ValidationStep<T> sourceStep) {}
}
