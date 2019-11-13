package io.sunshower.kernel.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.NonNull;
import lombok.val;

public class ValidationErrors {
  private final List<ValidationError> errors;

  ValidationErrors(List<ValidationError> errors) {
    this.errors = errors;
  }

  public ValidationErrors() {
    this(new ArrayList<>());
  }

  public void addError(ValidationError error) {
    this.errors.add(error);
  }

  public List<ValidationError> getErrors() {
    return Collections.unmodifiableList(errors);
  }

  public boolean hasErrors() {
    return !errors.isEmpty();
  }

  public static ValidationErrors empty() {
    return new ValidationErrors();
  }

  public ValidationErrors merge(@NonNull ValidationErrors other) {
    val result = new ArrayList<ValidationError>(errors.size() + other.errors.size());
    result.addAll(errors);
    result.addAll(other.errors);
    return new ValidationErrors(result);
  }

  public static ValidationErrors of(ValidationError... errors) {
    return new ValidationErrors(Arrays.asList(errors));
  }

  public void addAll(@NonNull ValidationErrors validate) {
    errors.addAll(validate.errors);
  }

  public boolean isEmpty() {
    return errors.isEmpty();
  }
}
