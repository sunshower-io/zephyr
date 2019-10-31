package io.sunshower.kernel.shell;

import io.sunshower.kernel.core.Kernel;
import io.sunshower.kernel.launch.KernelOptions;

public interface LauncherContext {

  Kernel getKernel();

  ShellConsole getConsole();

  KernelOptions getOptions();

  CommandRegistry getRegistry();
}
