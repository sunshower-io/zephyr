package io.sunshower.kernel.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import io.sunshower.kernel.test.Clean;
import io.sunshower.kernel.test.ModuleFilters;
import io.sunshower.kernel.test.ModuleLifecycleManager;
import io.sunshower.kernel.test.ZephyrTest;
import io.zephyr.api.ModuleContext;
import io.zephyr.api.ModuleEvents;
import io.zephyr.api.Queries;
import io.zephyr.cli.Zephyr;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.events.Event;
import io.zephyr.kernel.events.EventListener;
import javax.inject.Inject;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;

@ZephyrTest
@Clean(value = Clean.Mode.After, context = Clean.Context.Method)
public class ModuleTrackerTest {

  @Inject private Zephyr zephyr;
  @Inject private ModuleContext moduleContext;

  @Mock private EventListener<Module> moduleListener;
  @Inject private ModuleLifecycleManager lifecycleManager;

  @Test
  void ensureInstallingModuleProducesModuleEventWhenFilteredOnAll() {
    try (val tracker = moduleContext.createModuleTracker(t -> true)) {
      tracker.addEventListener(moduleListener, ModuleEvents.INSTALLED);
      zephyr.install(ProjectPlugins.TEST_PLUGIN_1.getUrl());
      verify(moduleListener, times(1)).onEvent(eq(ModuleEvents.INSTALLED), any());
    }
  }

  @Test
  void ensureInstallingModuleDoesNotCallModuleEventWhenFilteredOnNone() {
    try (val tracker = moduleContext.createModuleTracker(t -> false)) {
      tracker.addEventListener(moduleListener, ModuleEvents.INSTALLED);
      zephyr.install(ProjectPlugins.TEST_PLUGIN_1.getUrl());
      verify(moduleListener, times(0)).onEvent(eq(ModuleEvents.INSTALLED), any());
    }
  }

  @Test
  void ensureStartingModuleWorksAndProducesStartedEvent() {

    try (val tracker = moduleContext.createModuleTracker(t -> true)) {
      tracker.addEventListener(moduleListener, ModuleEvents.INSTALLED, ModuleEvents.STARTED);
      zephyr.install(ProjectPlugins.TEST_PLUGIN_1.getUrl());
      lifecycleManager.start(ModuleFilters.named("test-plugin-1"));
      tracker.waitUntil(t -> !t.isEmpty());
      verify(moduleListener, times(1)).onEvent(eq(ModuleEvents.INSTALLED), any());
      verify(moduleListener, times(1)).onEvent(eq(ModuleEvents.STARTED), any());
    }
  }

  @Test
  void ensureInstallingModuleWithExistingModuleProducesCorrectEventsForInstalledModule() {
    zephyr.install(ProjectPlugins.TEST_PLUGIN_1.getUrl());
    lifecycleManager.start(t -> true);

    try (val tracker = moduleContext.createModuleTracker(t -> true)) {
      tracker.addEventListener(moduleListener, ModuleEvents.INSTALLED);
      zephyr.install(ProjectPlugins.TEST_PLUGIN_2.getUrl());
      verify(moduleListener, times(2)).onEvent(eq(ModuleEvents.INSTALLED), any());
      Module expected =
          moduleContext.getModules(t -> t.getCoordinate().getName().equals("test-plugin-2")).get(0);
      verify(moduleListener, times(1))
          .onEvent(eq(ModuleEvents.INSTALLED), argThat(moduleMatcher(expected)));
    }
  }

  @Test
  void ensureNewlyInstalledModuleReceivesEventForPreviouslyInstalledModule() {
    zephyr.install(ProjectPlugins.TEST_PLUGIN_1.getUrl());
    lifecycleManager.start(t -> true);

    try (val tracker = moduleContext.createModuleTracker(t -> true)) {
      tracker.addEventListener(moduleListener, ModuleEvents.INSTALLED);
      zephyr.install(ProjectPlugins.TEST_PLUGIN_2.getUrl());
      verify(moduleListener, times(2)).onEvent(eq(ModuleEvents.INSTALLED), any());
      Module expected =
          moduleContext.getModules(t -> t.getCoordinate().getName().equals("test-plugin-1")).get(0);
      verify(moduleListener, times(1))
          .onEvent(eq(ModuleEvents.INSTALLED), argThat(moduleMatcher(expected)));
    }
  }

  @Test
  @SneakyThrows
  void ensureEventsAreProperlyDiscriminatedOnByType() {
    try (val tracker = moduleContext.createModuleTracker(t -> true)) {
      tracker.addEventListener(moduleListener, ModuleEvents.STARTED);
      zephyr.install(ProjectPlugins.TEST_PLUGIN_1.getUrl());
      lifecycleManager.start(ModuleFilters.named("test-plugin-1"));
      tracker.waitUntil(t -> !t.isEmpty());
      verify(moduleListener, times(0)).onEvent(eq(ModuleEvents.INSTALLED), any());
      verify(moduleListener, times(1)).onEvent(eq(ModuleEvents.STARTED), any());
    }
  }

  @Test
  void ensureEventListenerTracksCorrectModules() {
    try (val tracker = moduleContext.createModuleTracker(t -> true)) {
      tracker.addEventListener(moduleListener, ModuleEvents.STARTED);
      assertEquals(0, tracker.getTrackedCount(), "must have no tracked modules");
      zephyr.install(ProjectPlugins.TEST_PLUGIN_1.getUrl());
      lifecycleManager.start(ModuleFilters.named("test-plugin-1"));
      tracker.waitUntil(t -> !t.isEmpty());
      assertEquals(1, tracker.getTrackedCount(), "must have one tracked modules");
    }
  }

  @Test
  void ensureEventTrackerListenerTracksCorrectModulesUsingPredicate() {
    installExpressionLanguage();

    try (val tracker =
        moduleContext.createModuleTracker(
            m -> m.getCoordinate().getName().equals("test-plugin-1"))) {

      tracker.addEventListener(moduleListener, ModuleEvents.INSTALLED);
      assertEquals(0, tracker.getTrackedCount(), "must have no tracked modules");
      zephyr.install(ProjectPlugins.TEST_PLUGIN_1.getUrl());
      tracker.waitUntil(t -> !t.isEmpty());
      assertEquals(1, tracker.getTrackedCount(), "must have one tracked modules");
    }
  }

  @Test
  void ensureEventTrackerListenerTracksCorrectModulesUsingMVEL() {
    installExpressionLanguage();
    try (val tracker =
        moduleContext.createModuleTracker(
            Queries.create("mvel", "value.coordinate.name == 'test-plugin-1'"))) {
      tracker.addEventListener(moduleListener, ModuleEvents.INSTALLED);
      assertEquals(0, tracker.getTrackedCount(), "must have no tracked modules");
      zephyr.install(ProjectPlugins.TEST_PLUGIN_1.getUrl());
      tracker.waitUntil(t -> !t.isEmpty());
      assertEquals(1, tracker.getTrackedCount(), "must have one tracked modules");
    }
  }

  private void installExpressionLanguage() {
    zephyr.install(StandardModules.YAML.getUrl());
    zephyr.restart();
    zephyr.install(StandardModules.MVEL.getUrl());
    zephyr.restart();
  }

  static final ArgumentMatcher<Event<Module>> moduleMatcher(Module target) {
    return arg -> arg.getTarget().equals(target);
  }
}
