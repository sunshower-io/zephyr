package io.sunshower.kernel.concurrency;

public interface TaskStatus {

  TaskStatus UNRECOVERABLE = new DefaultStatus();

  final class DefaultStatus implements TaskStatus {}
}
