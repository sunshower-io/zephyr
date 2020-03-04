package io.zephyr.kernel.service;

import io.zephyr.api.*;
import io.zephyr.kernel.Coordinate;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.events.EventType;
import io.zephyr.kernel.events.Events;
import io.zephyr.kernel.log.Logging;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import lombok.NonNull;
import lombok.val;

@SuppressWarnings("PMD.DoNotUseThreads")
public class KernelServiceRegistry implements ServiceRegistry {

  static final Logger log = Logging.get(ServiceRegistry.class);
  /** private state */
  private Kernel kernel;

  /** immutable state */
  final Map<Coordinate, ModuleServiceRegistry> registries;

  public KernelServiceRegistry() {
    this.registries = new HashMap<>(0);
  }

  @Override
  public void close() {}

  @Override
  public void initialize(@NonNull Kernel kernel) {
    this.kernel = kernel;
  }

  @Override
  public Kernel getKernel() {
    return kernel;
  }

  @Override
  public <T> ServiceRegistration<T> register(Module module, ServiceDefinition<T> definition) {

    synchronized (registries) {
      val coordinate = module.getCoordinate();
      var registry = registries.get(coordinate);

      if (registry == null) {
        registry = new ModuleServiceRegistry(module, this);
        registries.put(coordinate, registry);
      }
      val reference = new DefaultServiceReference<T>(module, definition);
      val registration = new DefaultServiceRegistration<T>(reference, registry, definition);

      registry.register(registration);

      module
          .getTaskQueue()
          .schedule(
              new ServiceEventDispatchTask(ServiceEvents.REGISTERED, registration.getReference()));

      return registration;
    }
  }

  @Override
  public <T> void unregister(ServiceRegistration<T> definition) {
    definition.dispose();
  }

  @Override
  public ServiceRegistrationSet getRegistrations(Module module) {
    synchronized (registries) {
      val coordinate = module.getCoordinate();
      return registries.get(coordinate);
    }
  }

  void notifyServiceUnregistered(ServiceRegistration<?> registration) {
    synchronized (registries) {
      val ref = registration.getReference();
      val module = ref.getModule();
      val coordinate = module.getCoordinate();
      val moduleRegistry = registries.get(coordinate);

      if (moduleRegistry == null) {
        throw new IllegalStateException(
            "Attempting to notify a service event for a non-existing module '" + coordinate + "'");
      }
      if (moduleRegistry.registrations.isEmpty()) {
        registries.remove(coordinate);
      }
      module
          .getTaskQueue()
          .schedule(
              new ServiceEventDispatchTask(
                  ServiceEvents.UNREGISTERED, registration.getReference()));
    }
  }

  final class ServiceEventDispatchTask implements Runnable {

    private final EventType type;
    private final ServiceReference<?> registration;

    ServiceEventDispatchTask(EventType type, ServiceReference<?> registration) {
      this.type = type;
      this.registration = registration;
    }

    @Override
    public void run() {
      kernel.dispatchEvent(type, Events.create(registration));
    }
  }
}
