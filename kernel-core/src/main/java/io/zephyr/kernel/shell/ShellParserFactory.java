package io.zephyr.kernel.shell;

import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.launch.KernelOptions;

public interface ShellParserFactory {
  ShellParser create(Kernel kernel, KernelOptions options, CommandRegistry registry);
}
