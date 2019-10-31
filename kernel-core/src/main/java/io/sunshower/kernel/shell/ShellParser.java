package io.sunshower.kernel.shell;

import io.sunshower.kernel.launch.KernelOptions;

public interface ShellParser extends CommandRegistry {

  boolean perform(KernelOptions options);
}
