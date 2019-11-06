package io.sunshower.kernel.core;

import java.nio.file.FileSystem;
import java.util.List;

public interface Kernel {

  KernelLifecycle getLifecycle();

  ClassLoader getClassLoader();

  ModuleManager getModuleManager();

  <T> List<T> locateServices(Class<T> type);

  FileSystem getFileSystem();

  void start();

  void reload();

  void stop();
}
