package io.zephyr.api;

import io.zephyr.kernel.Module;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface ModuleContext {

  <T> T unwrap(Class<T> type);

  <T> Predicate<T> createFilter(Query<T> query);

  <T> CapabilityRegistration<T> provide(CapabilityDefinition<T> capability);

  <T> RequirementRegistration<T> createRequirement(Requirement<T> requirement);

  /** @return the current module */
  Module getModule();

  /**
   * return all modules matching the current filter
   *
   * @param filter
   * @return the modules matching the provided filter
   */
  List<Module> getModules(Predicate<Module> filter);

  /** */
  ModuleTracker trackModules(Predicate<Module> filter);

  ModuleTracker trackModules(Query<Module> filter);

  ServiceTracker trackServices(Query<ServiceReference<?>> filter);

  ServiceTracker trackServices(Predicate<ServiceReference<?>> filter);

  <T> ServiceRegistration<T> register(ServiceDefinition<T> definition);

  <T> ServiceRegistration<T> register(Class<T> type, String name, T value);

  <T> ServiceRegistration<T> register(Class<T> type, T value);

  <T> ServiceRegistration<T> register(Class<T> type, String name, Supplier<T> factory);

  <T> ServiceRegistration<T> register(Class<T> type, Supplier<T> factory);

  <T> List<ServiceReference<T>> getReferences(Class<T> type);

  List<ServiceReference<?>> getReferences(Query<ServiceDefinition<?>> query);
}
