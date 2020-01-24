package io.zephyr.kernel.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class KernelEventTypesTest {

  @Test
  void ensureKernelEventTypesHaveDifferentId() {
    assertNotEquals(
        KernelEventTypes.KERNEL_CLASSLOADER_CREATED,
        KernelEventTypes.KERNEL_FILESYSTEM_CREATED,
        "must not be same");
  }
}
