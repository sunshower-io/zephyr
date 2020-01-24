package io.zephyr.kernel.status;

import java.text.MessageFormat;

public enum StatusType {
  FAILED,
  PROGRESSING,
  SUCCEEDED,
  WARNING;

  public Status unresolvable(String message, Object... args) {
    return Status.unresolvable(this, MessageFormat.format(message, args));
  }

  public Status resolvable(String message) {
    return Status.resolvable(this, message);
  }
}
