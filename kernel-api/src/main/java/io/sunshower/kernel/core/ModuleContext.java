package io.sunshower.kernel.core;

public interface ModuleContext {

  void addModuleLifecycleListener(ModuleLifecycleListener l);

  void removeModuleLifecycleListener(ModuleLifecycleListener listener);
}
