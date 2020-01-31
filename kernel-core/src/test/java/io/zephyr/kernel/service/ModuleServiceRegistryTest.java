package io.zephyr.kernel.service;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import io.zephyr.api.ServiceReference;
import io.zephyr.api.ServiceRegistration;
import io.zephyr.kernel.Coordinate;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.TaskQueue;
import io.zephyr.kernel.core.ModuleCoordinate;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ModuleServiceRegistryTest {

  ModuleServiceRegistry registry;
  Coordinate coordinate = ModuleCoordinate.parse("hello:world:1.0.0-SNAPSHOT");
  @Mock Module module;
  @Mock TaskQueue taskQueue;
  @Mock ServiceReference<?> reference;
  @Mock ServiceRegistration registration;
  @Spy KernelServiceRegistry kernelRegistry;

  @BeforeEach
  void setUp() {
    val definition = new DefaultServiceDefinition<>(String.class, "supwab");
    registry = new ModuleServiceRegistry(module, kernelRegistry);

    given(registration.getReference()).willReturn(reference);
    given(reference.getModule()).willReturn(module);
    given(module.getCoordinate()).willReturn(coordinate);
    given(module.getTaskQueue()).willReturn(taskQueue);
    kernelRegistry.register(module, definition);
  }

  @Test
  void ensureKernelServiceRegistryUnregisterIsCalled() {
    registry.register(registration);
    registry.unregister(registration);
    verify(kernelRegistry).notifyServiceUnregistered(registration);
  }
}
