package io.zephyr.api;

public class Result {
  public static Result success() {
    return new Result();
  }

  public static Result failure() {
    return new Result();
  }
}
