package io.zephyr.logging;

import io.zephyr.kernel.extensions.EntryPoint;
import io.zephyr.kernel.launch.KernelOptions;
import java.util.logging.Logger;

public class KernelLauncher implements EntryPoint {
  @Override
  public Logger getLogger() {
    return Logger.getAnonymousLogger();
  }

  @Override
  public KernelOptions getOptions() {
    return new KernelOptions();
  }

  @Override
  public int getPriority() {
    return 0;
  }
}
