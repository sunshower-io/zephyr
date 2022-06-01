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
import io.zephyr.api.ServiceEvents;
import io.zephyr.api.ServiceReference;
import io.zephyr.cli.Zephyr;
import javax.inject.Inject;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.Mock;

@ZephyrTest
@Isolated
@Execution(ExecutionMode.SAME_THREAD)
@Clean(value = Clean.Mode.After, context = Clean.Context.Method)
public class ServiceTrackerTest {

  /** mocks */
  @Mock private EventListener<?> listener;

  /** state */
  @Inject private Zephyr zephyr;

  @Inject private ModuleContext context;
  @Inject private ModuleLifecycleManager lifecycleManager;

  @RepeatedTest(100)
  void ensureTrackingServiceProducesServiceInInstalledTestPlugin() {
    val tracker = context.trackServices(t -> true);
    tracker.addEventListener(listener, ServiceEvents.REGISTERED);
    tracker.addEventListener(
        (type, event) -> {
          try {
            val target = ((ServiceReference<?>) event.getTarget()).getDefinition().get();
            if (target.getClass().getName().contains("Service")) {
              val method = target.getClass().getDeclaredMethod("sayHello");
              method.invoke(target);
            }
          } catch (Exception ex) {
            ex.printStackTrace();
          }
          //          System.out.println(event.getTarget());
        },
        ServiceEvents.REGISTERED);
    zephyr.install(ProjectPlugins.TEST_PLUGIN_1.getUrl());
    zephyr.install(ProjectPlugins.TEST_PLUGIN_2.getUrl());
    lifecycleManager.start(t -> t.getCoordinate().getName().contains("2"));
    tracker.waitUntil(t -> t.size() > 0);
    verify(listener, timeout(1000).times(3)).onEvent(eq(ServiceEvents.REGISTERED), any());
    tracker.close();
    lifecycleManager.remove(t -> true);
  }

  @Test
  @SneakyThrows
  void ensureInstallingMvelWorks() {
    try {
      zephyr.install(StandardModules.MVEL.getUrl());
      zephyr.restart();
      zephyr.getKernel().getClassLoader().loadClass("org.mvel2.MVEL");
    } finally {
      zephyr.shutdown();
    }
  }
}
