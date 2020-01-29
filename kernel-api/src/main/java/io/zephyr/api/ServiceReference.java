package io.zephyr.api;

import io.zephyr.kernel.Module;
import java.util.List;

public interface ServiceReference<S> {

  Module getModule();

  List<Module> getDependentModules();

  boolean isAssignableTo(Module module, String className);
}
