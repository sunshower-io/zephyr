package io.sunshower.common;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.sunshower.kernel.core.Validatable;
import io.sunshower.kernel.core.ValidationError;
import java.util.Optional;
import lombok.val;
import org.junit.jupiter.api.Test;

class ChainedValidationStepTest {

  @Test
  void ensureValidationStepsAreCalled() {
    val cvs =
        new ChainedValidationStep<>(
            (a, b) -> {
              System.out.println("a");
              return Optional.of(new ValidationError());
            });

    cvs.add(
        (a, b) -> {
          System.out.println("b");
          return Optional.of(new ValidationError());
        });

    cvs.add(
        (a, b) -> {
          System.out.println("c");
          return Optional.of(new ValidationError());
        });

    cvs.add(
        (a, b) -> {
          System.out.println("d");
          return Optional.of(new ValidationError());
        });
    val validatable = mock(Validatable.class);
    cvs.validate(validatable, new Object());
    verify(validatable, times(4)).notify(any());
  }

  @Test
  void ensureValidationStepsAreCalledCorrectly() {

    val cvs =
        new ChainedValidationStep<>(
            (a, b) -> {
              System.out.println("a");
              return Optional.of(new ValidationError());
            });
    cvs.add((a, b) -> Optional.empty());
    val validatable = mock(Validatable.class);
    cvs.validate(validatable, new Object());
    verify(validatable, times(1)).notify(any());
  }
}
