package io.zephyr.kernel.shell;

import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.launch.KernelOptions;

public class DelegatingShellParserFactory implements ShellParserFactory {
  @Override
  public ShellParser create(Kernel kernel, KernelOptions options, CommandRegistry registry) {
    return new DelegatingShellParser(kernel, options, registry);
  }
}
