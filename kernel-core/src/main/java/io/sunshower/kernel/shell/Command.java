package io.sunshower.kernel.shell;

import io.sunshower.kernel.launch.KernelOptions;

public interface Command {
  void execute(String[] args, KernelOptions options);
}
