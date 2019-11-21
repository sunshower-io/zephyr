package io.zephyr.api;

import java.io.Serializable;

public class Parameters implements Serializable {

  final String[] arguments;

  protected Parameters(String... arguments) {
    this.arguments = arguments;
  }

  static final Parameters empty = new Parameters();

  public static Parameters empty() {
    return empty;
  }

  public static Parameters of(String... args) {
    return new Parameters(args);
  }

  public String[] formals() {
    return arguments;
  }
}
