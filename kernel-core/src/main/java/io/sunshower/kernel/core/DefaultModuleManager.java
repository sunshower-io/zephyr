package io.sunshower.kernel.core;

import io.sunshower.common.Collections;
import io.sunshower.kernel.Lifecycle;
import io.sunshower.kernel.Module;
import io.sunshower.kernel.status.Status;
import java.util.*;

public class DefaultModuleManager implements ModuleManager {
  private final Map<Module.Type, List<Module>> modules;

  public DefaultModuleManager() {
    modules = new EnumMap<>(Module.Type.class);
  }

  @Override
  public void addStatus(Status status) {}

  @Override
  public List<Module> getModules(Module.Type type) {
    return modules.computeIfAbsent(type, Collections::newList);
  }

  @Override
  public void resolve(Module module) {}

  @Override
  public void install(Module module) {
    module.getLifecycle().setState(Lifecycle.State.Installed);
    modules.computeIfAbsent(module.getType(), Collections::newList).add(module);
  }
}
