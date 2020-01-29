package io.zephyr.kernel.service;

import io.zephyr.kernel.KernelTestCase;
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class DefaultServiceRegistryLifecycleTest extends KernelTestCase {

  @Test
  void ensureRegistryIsSetOnKernel() {
    kernel.start();
    val reg = kernel.getServiceRegistry();
    assertNotNull(reg, "registry must not be null");
    kernel.stop();
  }

  @Test
  void ensureRegistryContainsKernelOnceKernelIsStarted() {
    assertNull(kernel.getServiceRegistry().getKernel(), "kernel must initially be null");
    kernel.start();
    assertNotNull(kernel.getServiceRegistry().getKernel(), "kernel must be set upon start");
    kernel.stop();
  }
}
