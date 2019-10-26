package io.sunshower.kernel.core;

import io.sunshower.kernel.Lifecycle;
import io.sunshower.kernel.Module;
import io.sunshower.kernel.status.StatusAware;
import java.util.List;

public interface ModuleManager extends StatusAware {

  void install(Module module);

  List<Module> getModules(Module.Type type);

  void resolve(Module module);

  LifecycleAction prepareFor(Lifecycle.State starting, Module dependent);
}
