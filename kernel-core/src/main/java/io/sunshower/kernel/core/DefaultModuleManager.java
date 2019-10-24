package io.sunshower.kernel.core;

import io.sunshower.kernel.Module;
import io.sunshower.kernel.status.Status;
import java.util.Collections;
import java.util.List;

public class DefaultModuleManager implements ModuleManager {
  @Override
  public void addStatus(Status status) {}

  @Override
  public List<Module> getModules(Module.Type type) {
    return Collections.emptyList();
  }
}
