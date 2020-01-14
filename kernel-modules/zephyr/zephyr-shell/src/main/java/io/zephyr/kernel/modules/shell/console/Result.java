package io.zephyr.kernel.modules.shell.console;

public class Result {
  public static Result success() {
    return new Result();
  }

  public static Result failure() {
    return new Result();
  }
}
