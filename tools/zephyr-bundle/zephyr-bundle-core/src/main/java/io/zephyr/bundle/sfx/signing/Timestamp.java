package io.zephyr.bundle.sfx.signing;

public interface Timestamp {

  enum Mode {
    RFC3161,
    Authenticode
  }

  Mode getMode();

  int getRetries();

  int getRetryWait();

  String getUrl();
}
