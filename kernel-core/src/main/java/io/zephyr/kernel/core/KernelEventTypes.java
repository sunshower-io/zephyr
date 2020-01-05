package io.zephyr.kernel.core;

import io.zephyr.kernel.events.EventType;

public enum KernelEventTypes implements EventType {

  /** global kernel start types */
  KERNEL_START_INITIATED,
  KERNEL_START_FAILED,
  KERNEL_START_SUCCEEDED,

  /** shutdown events */
  KERNEL_SHUTDOWN_INITIATED,
  KERNEL_SHUTDOWN_SUCCEEDED,

  /** task events */
  KERNEL_FILESYSTEM_CREATED,
  KERNEL_CLASSLOADER_CREATED,
  KERNEL_MODULE_LIST_READ;
}
