package io.zephyr.kernel.status;

import java.text.MessageFormat;
import java.util.Objects;
import lombok.NonNull;

public enum StatusType {
  FAILED,
  PROGRESSING,
  SUCCEEDED,
  WARNING;

  public Status unresolvable(@NonNull String message, Object... args) {
    return Status.unresolvable(
        this, MessageFormat.format(Objects.requireNonNullElse(message, "unknown"), args));
  }

  public Status resolvable(String message) {
    return Status.resolvable(this, message);
  }

  public Status unresolvable(Exception ex) {
    return Status.unresolvable(this, ex);
  }
}
