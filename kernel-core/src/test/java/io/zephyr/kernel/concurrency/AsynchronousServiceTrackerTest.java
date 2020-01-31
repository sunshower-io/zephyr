package io.zephyr.kernel.concurrency;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.zephyr.api.ServiceEvents;
import io.zephyr.kernel.service.DefaultServiceDefinition;
import io.zephyr.kernel.service.ServiceRegistryTestCase;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("PMD.DoNotUseThreads")
class AsynchronousServiceTrackerTest extends ServiceRegistryTestCase {

  @Test
  void ensureRegisteringServiceDispatchesRegisteredEvent() {
    val ctx = kernel.createContext(module);
    val tracker = ctx.trackServices(t -> true);
    tracker.addEventListener(listener, ServiceEvents.REGISTERED);
    listeners.add(listener);

    val registration =
        registry.register(
            module, new DefaultServiceDefinition<>(String.class, "hello-service", "whatever"));
    verify(taskQueue, times(2))
        .schedule(
            any(
                Runnable
                    .class)); // one of the events is scheduling the existingservicestask, the other
    // is the actual one
    registration.dispose();
    tracker.close();
  }
}
