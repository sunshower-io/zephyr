package io.zephyr.kernel.core;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ValidationError {
  final Object source;
  final Throwable error;

  public ValidationError() {
    this(null, null);
  }
}
