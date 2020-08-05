package io.zephyr.bundle.sfx.signing;

/** one of {KeySource, KeyStore} must be specified */
public interface KeySource {
  enum Type {
    FILE,
    PROPERTY,
    ENVIRONMENT
  }

  String getKeyPassword();
}
