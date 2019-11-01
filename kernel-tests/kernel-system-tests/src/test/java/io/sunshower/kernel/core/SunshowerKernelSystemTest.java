package io.sunshower.kernel.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.sunshower.kernel.test.KernelTest;
import javax.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

@KernelTest
public class SunshowerKernelSystemTest {
  @Inject private Kernel kernel;

  @Inject private KernelLifecycle kernelLifecycle;

  @Test
  void ensureKernelIsInjected() {
    assertNotNull(kernel, "kernel must not be null");
  }

  @Test
  void ensureKernelCanBeStarted() throws ExecutionException, InterruptedException {
    kernelLifecycle.start().get();
    assertEquals(kernelLifecycle.getState(), KernelLifecycle.State.Running, "Kernel must be running");
    kernelLifecycle.stop().get();
  }
}
