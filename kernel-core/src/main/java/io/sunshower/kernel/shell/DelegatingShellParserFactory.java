package io.sunshower.kernel.shell;

import io.sunshower.kernel.core.Kernel;
import io.sunshower.kernel.launch.KernelOptions;

public class DelegatingShellParserFactory implements ShellParserFactory {
  @Override
  public ShellParser create(Kernel kernel, KernelOptions options) {
    return new DelegatingShellParser(kernel, options);
  }
}
