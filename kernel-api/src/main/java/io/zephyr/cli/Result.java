package io.zephyr.cli;

public class Result {
  public static Result success() {
    return new Result();
  }

  public static Result failure() {
    return new Result();
  }
}
