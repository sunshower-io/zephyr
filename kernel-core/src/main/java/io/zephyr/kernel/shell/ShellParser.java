package io.zephyr.kernel.shell;

import io.zephyr.kernel.launch.KernelOptions;

@SuppressWarnings("PMD.UseVarargs")
public interface ShellParser {

  boolean perform(KernelOptions options, String[] rest) throws Exception;
}
