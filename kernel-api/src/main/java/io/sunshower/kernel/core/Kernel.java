package io.sunshower.kernel.core;

import java.util.List;

public interface Kernel {

  ClassLoader getClassLoader();

  ModuleManager getModuleManager();

  <T> List<T> locateServices(Class<T> type);
}
