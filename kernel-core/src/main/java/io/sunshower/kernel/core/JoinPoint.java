package io.sunshower.kernel.core;

import io.sunshower.gyre.Scope;
import io.sunshower.kernel.concurrency.Task;
import java.util.UUID;

public class JoinPoint extends Task {
  public JoinPoint(String name) {
    super(name);
  }

  @Override
  public TaskValue run(Scope scope) {
    return null;
  }

  public static Task newJoinPoint() {
    return new JoinPoint(UUID.randomUUID().toString());
  }
}
