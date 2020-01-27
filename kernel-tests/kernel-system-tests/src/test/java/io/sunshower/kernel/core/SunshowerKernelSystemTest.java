package io.sunshower.kernel.core;

import io.sunshower.kernel.test.ZephyrTest;
import io.zephyr.api.ModuleEvents;
import io.zephyr.cli.Zephyr;
import io.zephyr.kernel.Lifecycle;
import io.zephyr.kernel.core.Kernel;
import javax.inject.Inject;

import io.zephyr.kernel.core.KernelLifecycle;
import io.zephyr.kernel.events.EventListener;
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@ZephyrTest
class SunshowerKernelSystemTest {
  @Inject private Kernel kernel;
  @Inject private Zephyr zephyr;

  @Test
  void ensureKernelIsInjected() {
    assertNotNull(kernel, "kernel must not be null");
  }

  @Test
  void ensureReloadingWorks() {
    kernel.start();
    kernel.stop();
  }

  @Test
  void ensureStoppedKernelHasNoEventListeners() {
    assertNotEquals(
        KernelLifecycle.State.Running, kernel.getLifecycle().getState(), "must not be running");
    assertEquals(0, kernel.getListenerCount(), "must have no listeners");
  }

  @Test
  void ensureRunningKernelHasCorrectNumberOfListeners() {
    kernel.start();
    assertEquals(0, kernel.getListenerCount(), "must have no listeners");
  }

  @Test
  void ensureListenerCountIsZeroAfterKernelModuleInstallation() throws InterruptedException {
    kernel.start();
    val listener = mock(EventListener.class);
    kernel.addEventListener(listener, EventListener.Options.REMOVE_AFTER_DISPATCH, ModuleEvents.INSTALLED);
    zephyr.install(StandardModules.YAML.getUrl());
    assertEquals(0, kernel.getListenerCount(), "must have no listeners");
    kernel.stop();

  }
}
