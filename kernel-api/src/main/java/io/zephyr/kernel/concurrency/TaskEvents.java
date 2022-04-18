package io.zephyr.kernel.concurrency;

import static io.zephyr.kernel.status.StatusType.FAILED;
import static io.zephyr.kernel.status.StatusType.PROGRESSING;
import static io.zephyr.kernel.status.StatusType.SUCCEEDED;

import io.sunshower.lang.events.EventType;
import io.zephyr.kernel.status.StatusType;

public enum TaskEvents implements TaskEventType, EventType {
  PROCESS_STARTING(PROGRESSING),
  PROCESS_COMPLETE(SUCCEEDED),
  PROCESS_ERROR(FAILED),

  TASK_STARTING(PROGRESSING),
  TASK_COMPLETE(SUCCEEDED),
  TASK_ERROR(FAILED),

  TASK_PHASE_ERROR(FAILED),
  TASK_PHASE_STARTING(PROGRESSING),
  TASK_PHASE_COMPLETE(SUCCEEDED);

  private final int id;
  private final StatusType statusType;

  TaskEvents(final StatusType statusType) {
    this.id = EventType.newId();
    this.statusType = statusType;
  }

  @Override
  public StatusType getStatusType() {
    return statusType;
  }

  @Override
  public int getId() {
    return id;
  }
}
