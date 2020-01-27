package io.zephyr.api;

import io.zephyr.kernel.Module;
import java.util.List;
import java.util.function.Predicate;

public interface ModuleContext {

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
  ModuleTracker createModuleTracker(Predicate<Module> filter);

  ModuleTracker createModuleTracker(Query<Module> filter );
}
