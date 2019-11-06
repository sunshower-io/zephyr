package io.sunshower.kernel.concurrency;

public enum TaskEvents implements TaskEventType {
  PROCESS_STARTING,
  PROCESS_COMPLETE,
  PROCESS_ERROR,

  TASK_STARTING,
  TASK_COMPLETE,
  TASK_ERROR,

  TASK_PHASE_ERROR,
  TASK_PHASE_STARTING,
  TASK_PHASE_COMPLETE
}
