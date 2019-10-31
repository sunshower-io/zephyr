package io.sunshower.kernel.shell;

import io.sunshower.kernel.core.Kernel;
import io.sunshower.kernel.launch.KernelOptions;
import lombok.Getter;
import lombok.Setter;

public class KernelLauncherContext implements LauncherContext {
  @Setter private Kernel kernel;
  @Getter final ShellConsole console;
  final CommandRegistry registry;
  final KernelOptions kernelOptions;

  public KernelLauncherContext(
      final ShellConsole console,
      final CommandRegistry registry,
      final KernelOptions kernelOptions) {
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
