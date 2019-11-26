package io.zephyr.kernel.core;

import io.sunshower.gyre.Scope;
import io.zephyr.kernel.concurrency.Task;
import java.util.UUID;

public class JoinPoint extends Task {
  public JoinPoint(String name) {
    super(name);
  }

  @Override
  public TaskValue run(Scope scope) {
    return null;
  }

  public static Task newJoinPoint(String name) {
    return new JoinPoint(name);
  }

  public static Task newJoinPoint() {
    return new JoinPoint(UUID.randomUUID().toString());
  }
}
