package io.zephyr.kernel.status;

public enum StatusType {
  FAILED,
  PROGRESSING,
  SUCCEEDED,
  WARNING;


  public Status unresolvable(String message) {
    return Status.unresolvable(this, message);
  }

  public Status resolvable(String message) {
    return Status.resolvable(this, message);
  }
}
