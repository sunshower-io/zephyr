package io.zephyr.kernel.status;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;

public class Status {
  final String message;
  final StatusType type;
  final boolean resolvable;
  final Optional<Throwable> cause;
  final List<Resolution> resolutions;

  public Status(StatusType type, String message, boolean resolvable) {
    this(type, message, resolvable, Optional.empty());
  }

  public Status(StatusType type, String message, boolean resolvable, Optional<Throwable> cause) {
    this.type = type;
    this.cause = cause;
    this.message = message;
    this.resolvable = resolvable;
    this.resolutions = new ArrayList<>();
  }

  public static Status unresolvable(StatusType statusType, Exception ex) {
    return new Status(statusType, ex.getMessage(), false, Optional.ofNullable(ex));
  }

  public void addResolution(@NonNull Resolution resolution) {
    resolutions.add(resolution);
  }

  public boolean isResolvable() {
    return resolvable;
  }

  public StatusException toException() {
    return new StatusException(this);
  }

  public static Status resolvable(StatusType type, String message) {
    return new Status(type, message, true);
  }

  public static Status unresolvable(StatusType type, String message) {
    return new Status(type, message, false);
  }
}
