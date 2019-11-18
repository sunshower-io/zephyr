package io.zephyr.kernel.shell;

import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.launch.KernelOptions;

public interface LauncherContext {

  Kernel getKernel();

  ShellConsole getConsole();

  KernelOptions getOptions();

  CommandRegistry getRegistry();
}
