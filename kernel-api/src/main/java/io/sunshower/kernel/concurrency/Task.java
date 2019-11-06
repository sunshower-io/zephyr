package io.sunshower.kernel.concurrency;

import lombok.AllArgsConstructor;

public interface Task {

  TaskValue run(Context context);

  @AllArgsConstructor
  final class TaskValue {
    final Object value;
    final String name;
  }
}
