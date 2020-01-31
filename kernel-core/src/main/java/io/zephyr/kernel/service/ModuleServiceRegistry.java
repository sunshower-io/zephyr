package io.zephyr.kernel.service;

import io.zephyr.api.ServiceRegistration;
import io.zephyr.api.ServiceRegistrationSet;
import io.zephyr.kernel.Module;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.val;

final class ModuleServiceRegistry implements ServiceRegistrationSet {
  final Module module;
  final KernelServiceRegistry registry;
  final List<ServiceRegistration<?>> registrations;

  public ModuleServiceRegistry(Module module, KernelServiceRegistry registry) {
    this.module = module;
    this.registry = registry;
    this.registrations = new ArrayList<>(0);
  }

  public <T> void register(ServiceRegistration<T> registration) {
    synchronized (registrations) {
      registrations.add(registration);
    }
  }

  @SuppressWarnings("PMD.CompareObjectsWithEquals")
  public <T> void unregister(ServiceRegistration<T> registration) {
    synchronized (registrations) {
      val iter = registrations.iterator();
      while (iter.hasNext()) {
        val next = iter.next();
        if (next == registration) {
          iter.remove();
          registry.notifyServiceUnregistered(next);
        }
      }
    }
  }

  @Override
  public Collection<ServiceRegistration<?>> getRegistrations() {
    return registrations;
  }
}
