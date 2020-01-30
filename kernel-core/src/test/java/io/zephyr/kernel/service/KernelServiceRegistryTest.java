package io.zephyr.kernel.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.zephyr.api.ServiceEvents;
import io.zephyr.api.ServiceRegistry;
import io.zephyr.kernel.KernelTestCase;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.TaskQueue;
import io.zephyr.kernel.events.EventListener;
import io.zephyr.kernel.events.EventType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"PMD.DoNotUseThreads", "PMD.JUnitTestContainsTooManyAsserts"})
class KernelServiceRegistryTest extends KernelTestCase {

  private ServiceRegistry registry;
  private List<EventListener<?>> listeners;

  /** mocks */
  @Mock private Module module;

  @Mock private TaskQueue taskQueue;

  @Mock private EventListener<?> listener;

  @Override
  @BeforeEach
  protected void setUp() {
    listeners = new ArrayList<>();
    super.setUp();
    kernel.start();
    registry = kernel.getServiceRegistry();
  }

  @Override
  @AfterEach
  protected void tearDown() throws IOException {
    for (val listener : listeners) {
      kernel.removeEventListener(listener);
    }
    kernel.stop();
    assertEquals(0, kernel.getListenerCount(), "must have no more listeners");
    super.tearDown();
  }

  @Test
  void ensureRegistryDispatchesCorrectEventForServiceRegistration() {
    given(module.getTaskQueue()).willReturn(taskQueue);
    registerListener(listener, ServiceEvents.REGISTERED);
    registry.register(
        module, new DefaultServiceDefinition<>(String.class, "hello-service", "whatever"));
    verify(taskQueue, times(1)).schedule(any(Runnable.class));
  }

  @Test
  void ensureRegistryDispatchesCorrectEventForServiceRegistrationAndUnregistration() {
    given(module.getTaskQueue()).willReturn(taskQueue);
    registerListener(listener, ServiceEvents.REGISTERED, ServiceEvents.UNREGISTERED);

    val registration =
        registry.register(
            module, new DefaultServiceDefinition<>(String.class, "hello-service", "whatever"));
    registration.dispose();
    verify(taskQueue, times(2)).schedule(any(Runnable.class));
  }

  @Test
  void ensureRegistryIsEmptyAfterFinalServiceUnregistration() {
    given(module.getTaskQueue()).willReturn(taskQueue);
    registerListener(listener, ServiceEvents.REGISTERED, ServiceEvents.UNREGISTERED);

    val registration =
        registry.register(
            module, new DefaultServiceDefinition<>(String.class, "hello-service", "whatever"));
    val kserviceReg = (KernelServiceRegistry) registry;
    assertFalse(kserviceReg.registries.isEmpty(), "Must have at least one service");

    registration.dispose();

    assertTrue(kserviceReg.registries.isEmpty(), "must have no registered services");
  }

  private void registerListener(EventListener<?> listener, EventType... types) {
    kernel.addEventListener(listener, types);
    listeners.add(listener);
  }
}
