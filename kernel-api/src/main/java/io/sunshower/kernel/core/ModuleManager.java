package io.sunshower.kernel.core;

import io.sunshower.kernel.Module;
import io.sunshower.kernel.status.StatusAware;
import java.util.List;

public interface ModuleManager extends StatusAware {

  void resolve(Module module);

  List<Module> getModules(Module.Type type);
}
