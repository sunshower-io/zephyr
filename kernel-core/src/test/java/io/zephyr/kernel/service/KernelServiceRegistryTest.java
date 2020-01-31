package io.zephyr.kernel.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.zephyr.api.ServiceEvents;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({
  "PMD.DoNotUseThreads",
  "PMD.JUnitTestContainsTooManyAsserts",
  "PMD.AvoidDuplicateLiterals"
})
class KernelServiceRegistryTest extends ServiceRegistryTestCase {

  /** mocks */
  @Test
  void ensureRegistryDispatchesCorrectEventForServiceRegistration() {
    registerListener(listener, ServiceEvents.REGISTERED);
    registry.register(
        module, new DefaultServiceDefinition<>(String.class, "hello-service", "whatever"));
    verify(taskQueue, times(1)).schedule(any(Runnable.class));
  }

  @Test
  void ensureRegistryDispatchesCorrectEventForServiceRegistrationAndUnregistration() {
    registerListener(listener, ServiceEvents.REGISTERED, ServiceEvents.UNREGISTERED);

    val registration =
        registry.register(
            module, new DefaultServiceDefinition<>(String.class, "hello-service", "whatever"));
    registration.dispose();
    verify(taskQueue, times(2)).schedule(any(Runnable.class));
  }

  @Test
  void ensureRegistryIsEmptyAfterFinalServiceUnregistration() {
    registerListener(listener, ServiceEvents.REGISTERED, ServiceEvents.UNREGISTERED);

    val registration =
        registry.register(
            module, new DefaultServiceDefinition<>(String.class, "hello-service", "whatever"));
    val kserviceReg = (KernelServiceRegistry) registry;
    assertFalse(kserviceReg.registries.isEmpty(), "Must have at least one service");

    registration.dispose();

    assertTrue(kserviceReg.registries.isEmpty(), "must have no registered services");
  }

  @Test
  void ensureModuleServiceRegistryRemovesServiceUponDisposal() {

    registerListener(listener, ServiceEvents.REGISTERED, ServiceEvents.UNREGISTERED);

    val registration =
        registry.register(
            module, new DefaultServiceDefinition<>(String.class, "hello-service", "whatever"));

    registry.register(
        module, new DefaultServiceDefinition<>(String.class, "world-service", "whatever2"));
    val kserviceReg = (KernelServiceRegistry) registry;
    val moduleRegistry = kserviceReg.registries.values().iterator().next();
    assertEquals(2, moduleRegistry.registrations.size(), "must have 2 registrations");

    registration.dispose();

    assertEquals(1, moduleRegistry.registrations.size(), "must have 1 registration");
  }

  @Test
  void ensureModuleDisposalRemovesCorrectService() {

    registerListener(listener, ServiceEvents.REGISTERED, ServiceEvents.UNREGISTERED);

    val registration =
        registry.register(
            module, new DefaultServiceDefinition<>(String.class, "hello-service", "whatever"));

    val reg2 =
        registry.register(
            module, new DefaultServiceDefinition<>(String.class, "world-service", "whatever2"));
    val kserviceReg = (KernelServiceRegistry) registry;
    val moduleRegistry = kserviceReg.registries.values().iterator().next();
    assertEquals(2, moduleRegistry.registrations.size(), "must have 2 registrations");

    registration.dispose();
    assertTrue(moduleRegistry.registrations.contains(reg2), "must remove correct registration");
  }
}
