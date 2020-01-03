package io.zephyr;

import io.zephyr.kernel.Module;
import java.util.List;
import java.util.function.Predicate;

public interface PluginContext {

  <T> RequirementRegistration<T> createRequirement(Requirement<T> requirement);

  <T> CapabilityRegistration<T> provide(CapabilityDefinition<T> capability);

  /** @return the current module */
  Module getModule();

  /**
   * return all modules matching the current filter
   *
   * @param filter
   * @return the modules matching the provided filter
   */
  List<Module> getModules(Predicate<Module> filter);
}
