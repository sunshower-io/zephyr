package io.zephyr.kernel.shell;

import io.zephyr.kernel.launch.KernelOptions;

public interface ShellParser {

  boolean perform(KernelOptions options, String[] rest) throws Exception;
}
