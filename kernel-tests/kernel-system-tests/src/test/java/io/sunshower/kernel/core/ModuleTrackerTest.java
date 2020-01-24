package io.sunshower.kernel.core;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.sunshower.kernel.test.ZephyrTest;
import io.zephyr.api.ModuleContext;
import io.zephyr.api.ModuleEvents;
import io.zephyr.cli.Zephyr;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.events.EventListener;
import javax.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

@ZephyrTest
public class ModuleTrackerTest {

  @Inject private Zephyr zephyr;
  @Inject private Kernel kernel;
  @Inject private ModuleContext moduleContext;

  @Mock private EventListener<Module> moduleListener;

  @Test
  void ensureInstallingModuleProducesModuleEventWhenFilteredOnAll() {
    moduleContext
        .createModuleTracker(t -> true)
        .addEventListener(moduleListener, ModuleEvents.INSTALLED);
    zephyr.install(ProjectPlugins.TEST_PLUGIN_1.getUrl());
    verify(moduleListener, times(1)).onEvent(eq(ModuleEvents.INSTALLED), any());
  }

  @Test
  void ensureInstallingModuleDoesNotCallModuleEventWhenFilteredOnNone() {
    moduleContext
        .createModuleTracker(t -> false)
        .addEventListener(moduleListener, ModuleEvents.INSTALLED);
    zephyr.install(ProjectPlugins.TEST_PLUGIN_1.getUrl());
    verify(moduleListener, times(0)).onEvent(eq(ModuleEvents.INSTALLED), any());
  }
}
