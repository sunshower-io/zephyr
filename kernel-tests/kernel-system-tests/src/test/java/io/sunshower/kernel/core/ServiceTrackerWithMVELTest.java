package io.sunshower.kernel.core;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import io.sunshower.kernel.test.Clean;
import io.sunshower.kernel.test.ModuleLifecycleManager;
import io.sunshower.kernel.test.ZephyrTest;
import io.sunshower.lang.events.EventListener;
import io.zephyr.api.ModuleContext;
import io.zephyr.api.Queries;
import io.zephyr.api.ServiceEvents;
import io.zephyr.cli.Zephyr;
import javax.inject.Inject;
import lombok.val;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.Mock;

@ZephyrTest
@Isolated
@Execution(ExecutionMode.SAME_THREAD)
@Clean(value = Clean.Mode.After, context = Clean.Context.Method)
public class ServiceTrackerWithMVELTest {

  /** mocks */
  @Mock private EventListener<?> listener;

  /** state */
  @Inject private Zephyr zephyr;

  @Inject private ModuleContext context;
  @Inject private ModuleLifecycleManager lifecycleManager;

  @RepeatedTest(100)
  void ensureTrackingServiceProducesServiceInInstalledTestPluginWithMVELFilter() {
    zephyr.startup();
    try {
      zephyr.install(StandardModules.MVEL.getUrl());
      zephyr.restart();

      val tracker =
          context.trackServices(Queries.create("mvel", "value.definition.name contains 'Service'"));
      tracker.addEventListener(listener, ServiceEvents.REGISTERED);
      zephyr.install(ProjectPlugins.TEST_PLUGIN_1.getUrl());

      zephyr.install(ProjectPlugins.TEST_PLUGIN_2.getUrl());
      lifecycleManager.start(t -> t.getCoordinate().getName().contains("2"));
      tracker.waitUntil(t -> t.size() > 0);
      verify(listener, timeout(1000).times(3)).onEvent(eq(ServiceEvents.REGISTERED), any());
      tracker.close();

      lifecycleManager.remove(t -> true);
    } finally {
      zephyr.shutdown();
    }
  }
}
