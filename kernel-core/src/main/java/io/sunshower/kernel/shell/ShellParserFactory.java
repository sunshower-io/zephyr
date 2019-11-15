package io.sunshower.kernel.shell;

import io.sunshower.kernel.core.Kernel;
import io.sunshower.kernel.launch.KernelOptions;

public interface ShellParserFactory {
  ShellParser create(Kernel kernel, KernelOptions options);
}
