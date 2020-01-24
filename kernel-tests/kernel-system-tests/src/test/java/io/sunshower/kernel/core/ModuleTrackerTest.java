package io.sunshower.kernel.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.sunshower.kernel.test.ModuleFilters;
import io.sunshower.kernel.test.ModuleLifecycleManager;
import io.sunshower.kernel.test.ZephyrTest;
import io.zephyr.api.ModuleContext;
import io.zephyr.api.ModuleEvents;
import io.zephyr.cli.Zephyr;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.events.EventListener;
import javax.inject.Inject;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.test.annotation.DirtiesContext;

@ZephyrTest
@DirtiesContext(
  classMode = DirtiesContext.ClassMode.AFTER_CLASS,
  hierarchyMode = DirtiesContext.HierarchyMode.EXHAUSTIVE
)
public class ModuleTrackerTest {

  @Inject private Zephyr zephyr;
  @Inject private ModuleContext moduleContext;

  @Mock private EventListener<Module> moduleListener;
  @Inject private ModuleLifecycleManager lifecycleManager;

  @Test
  void ensureInstallingModuleProducesModuleEventWhenFilteredOnAll() throws Exception {
    try (val tracker = moduleContext.createModuleTracker(t -> true)) {
      tracker.addEventListener(moduleListener, ModuleEvents.INSTALLED);
      zephyr.install(ProjectPlugins.TEST_PLUGIN_1.getUrl());
      verify(moduleListener, times(1)).onEvent(eq(ModuleEvents.INSTALLED), any());
    }
  }

  @Test
  void ensureInstallingModuleDoesNotCallModuleEventWhenFilteredOnNone() throws Exception {
    try (val tracker = moduleContext.createModuleTracker(t -> false)) {
      tracker.addEventListener(moduleListener, ModuleEvents.INSTALLED);
      zephyr.install(ProjectPlugins.TEST_PLUGIN_1.getUrl());
      verify(moduleListener, times(0)).onEvent(eq(ModuleEvents.INSTALLED), any());
    }
  }

  @Test
  void ensureStartingModuleWorksAndProducesStartedEvent() throws Exception {

    try (val tracker = moduleContext.createModuleTracker(t -> true)) {
      tracker.addEventListener(moduleListener, ModuleEvents.INSTALLED, ModuleEvents.STARTED);
      zephyr.install(ProjectPlugins.TEST_PLUGIN_1.getUrl());
      lifecycleManager.start(ModuleFilters.named("test-plugin-1"));
      verify(moduleListener, times(1)).onEvent(eq(ModuleEvents.INSTALLED), any());
      verify(moduleListener, times(1)).onEvent(eq(ModuleEvents.STARTED), any());
    }
  }

  @Test
  void ensureEventsAreProperlyDiscriminatedOnByType() throws Exception {
    try (val tracker = moduleContext.createModuleTracker(t -> true)) {
      tracker.addEventListener(moduleListener, ModuleEvents.STARTED);
      zephyr.install(ProjectPlugins.TEST_PLUGIN_1.getUrl());
      lifecycleManager.start(ModuleFilters.named("test-plugin-1"));
      verify(moduleListener, times(0)).onEvent(eq(ModuleEvents.INSTALLED), any());
      verify(moduleListener, times(1)).onEvent(eq(ModuleEvents.STARTED), any());
    }
  }

  @Test
  void ensureEventListenerTracksCorrectModules() throws Exception {
    try (val tracker = moduleContext.createModuleTracker(t -> true)) {
      tracker.addEventListener(moduleListener, ModuleEvents.STARTED);
      assertEquals(0, tracker.getTrackedCount(), "must have no tracked modules");
      zephyr.install(ProjectPlugins.TEST_PLUGIN_1.getUrl());
      lifecycleManager.start(ModuleFilters.named("test-plugin-1"));
      tracker.waitUntil(t -> !t.isEmpty());
      assertEquals(1, tracker.getTrackedCount(), "must have one tracked modules");
    }
  }
}
