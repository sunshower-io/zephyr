package io.sunshower.kernel.status;

import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

public class Status {
  final String message;
  final StatusType type;
  final boolean resolvable;
  final List<Resolution> resolutions;

  public Status(StatusType type, String message, boolean resolvable) {
    this.type = type;
    this.message = message;
    this.resolvable = resolvable;
    resolutions = new ArrayList<>();
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
}
