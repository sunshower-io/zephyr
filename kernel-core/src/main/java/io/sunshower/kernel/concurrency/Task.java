package io.sunshower.kernel.concurrency;

import lombok.AllArgsConstructor;

public interface Task<T> {

  TaskValue<T> run(Context context);

  @AllArgsConstructor
  final class TaskValue<T> {
    final T value;
    final String name;
  }
}
