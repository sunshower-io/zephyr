package io.zephyr.api;

import io.zephyr.kernel.core.Kernel;

public interface CommandContext {

  <T> T getService(Class<T> service);
  Kernel getKernel();
}
