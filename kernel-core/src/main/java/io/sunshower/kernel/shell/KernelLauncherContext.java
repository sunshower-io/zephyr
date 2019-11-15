package io.sunshower.kernel.shell;

import io.sunshower.kernel.core.Kernel;
import io.sunshower.kernel.launch.KernelOptions;
import lombok.Getter;

public class KernelLauncherContext implements LauncherContext {
  @Getter final ShellConsole console;
  final CommandRegistry registry;
  final KernelOptions kernelOptions;
  private final Kernel kernel;

  public KernelLauncherContext(
      final Kernel kernel,
      final ShellConsole console,
      final CommandRegistry registry,
      final KernelOptions kernelOptions) {
    this.kernel = kernel;
    this.console = console;
    this.registry = registry;
    this.kernelOptions = kernelOptions;
  }

  @Override
  public Kernel getKernel() {
    return kernel;
  }

  @Override
  public KernelOptions getOptions() {
    return kernelOptions;
  }

  @Override
  public CommandRegistry getRegistry() {
    return registry;
  }
}
