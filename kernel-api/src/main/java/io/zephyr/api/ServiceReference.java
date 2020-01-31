package io.zephyr.api;

import io.zephyr.kernel.Module;
import java.util.List;

public interface ServiceReference<S> {

  Module getModule();

  List<Module> getDependentModules();

  ServiceDefinition<S> getDefinition();

  boolean isAssignableTo(Module module, String className);
}
