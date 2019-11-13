package io.sunshower.kernel.core;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.sunshower.kernel.test.KernelTest;
import javax.inject.Inject;
import org.junit.jupiter.api.Test;

@KernelTest
public class SunshowerKernelSystemTest {
  @Inject private Kernel kernel;

  @Test
  void ensureKernelIsInjected() {
    assertNotNull(kernel, "kernel must not be null");
  }

  @Test
  void ensureReloadingWorks() {
    kernel.start();
    kernel.stop();
  }



}
