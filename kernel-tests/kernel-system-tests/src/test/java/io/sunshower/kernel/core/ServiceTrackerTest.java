package io.sunshower.kernel.core;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import io.sunshower.kernel.test.Clean;
import io.sunshower.kernel.test.ModuleLifecycleManager;
import io.sunshower.kernel.test.ZephyrTest;
import io.zephyr.api.ModuleContext;
import io.zephyr.api.Queries;
import io.zephyr.api.ServiceEvents;
import io.zephyr.cli.Zephyr;
import io.zephyr.kernel.events.EventListener;
import javax.inject.Inject;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

@ZephyrTest
@Clean(value = Clean.Mode.After, context = Clean.Context.Method)
public class ServiceTrackerTest {
  /** mocks */
  @Mock private EventListener<?> listener;

  /** state */
  @Inject private Zephyr zephyr;

  @Inject private ModuleContext context;
  @Inject private ModuleLifecycleManager lifecycleManager;

  @Test
  void ensureTrackingServiceProducesServiceInInstalledTestPlugin() {
    val tracker = context.trackServices(t -> true);
    tracker.addEventListener(listener, ServiceEvents.REGISTERED);
    zephyr.install(ProjectPlugins.TEST_PLUGIN_2.getUrl(), ProjectPlugins.TEST_PLUGIN_1.getUrl());
    lifecycleManager.start(t -> t.getCoordinate().getName().contains("2"));
    tracker.waitUntil(t -> t.size() > 0);
    verify(listener).onEvent(eq(ServiceEvents.REGISTERED), any());
    tracker.close();
    lifecycleManager.remove(t -> true);
  }

  @Test
  void ensureTrackingServiceProducesServiceInInstalledTestPluginWithMVELFilter() {
    zephyr.install(StandardModules.MVEL.getUrl());
    zephyr.restart();

    val tracker =
        context.trackServices(Queries.create("mvel", "value.definition.name contains 'class'"));
    tracker.addEventListener(listener, ServiceEvents.REGISTERED);
    zephyr.install(ProjectPlugins.TEST_PLUGIN_2.getUrl(), ProjectPlugins.TEST_PLUGIN_1.getUrl());
    lifecycleManager.start(t -> t.getCoordinate().getName().contains("2"));
    tracker.waitUntil(t -> t.size() > 0);
    verify(listener, timeout(1000)).onEvent(eq(ServiceEvents.REGISTERED), any());
    tracker.close();

    lifecycleManager.remove(t -> true);
  }
}
