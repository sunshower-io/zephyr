package io.zephyr.kernel.command;

import io.zephyr.api.CommandContext;
import io.zephyr.kernel.core.Kernel;

public class DefaultCommandContext implements CommandContext {

  final Kernel kernel;

  public DefaultCommandContext(Kernel kernel) {
    this.kernel = kernel;
  }

  @Override
  public Kernel getKernel() {
    return kernel;
  }
}
