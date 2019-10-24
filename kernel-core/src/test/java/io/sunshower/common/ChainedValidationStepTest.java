package io.sunshower.common;

import static io.sunshower.kernel.core.ValidationErrors.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.sunshower.kernel.core.Validatable;
import io.sunshower.kernel.core.ValidationError;
import lombok.val;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unchecked")
class ChainedValidationStepTest {

  @Test
  void ensureValidationStepsAreCalled() {
    val cvs = new ChainedValidationStep<>((a, b) -> of(new ValidationError()));

    cvs.add((a, b) -> of(new ValidationError()));

    cvs.add((a, b) -> of(new ValidationError()));

    cvs.add((a, b) -> of(new ValidationError()));
    val validatable = mock(Validatable.class);
    cvs.validate(validatable, new Object());
    verify(validatable, times(4)).notify(any(), any());
  }

  @Test
  void ensureValidationStepsAreCalledCorrectly() {

    val cvs = new ChainedValidationStep<>((a, b) -> of(new ValidationError()));
    cvs.add((a, b) -> empty());
    val validatable = mock(Validatable.class);
    cvs.validate(validatable, new Object());
    verify(validatable, times(1)).notify(any(), any());
  }

  @Test
  void verifyNoValidationStepsAreCalledWhenYieldIsEmpty() {
    val cvs = new ChainedValidationStep<>((a, b) -> empty());

    val validatable = mock(Validatable.class);
    cvs.validate(validatable, new Object());
    verify(validatable, never()).notify(any(), any());
  }
}
