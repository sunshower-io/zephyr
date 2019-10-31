package io.sunshower.kernel.shell.commands;

import io.sunshower.kernel.shell.LauncherContext;
import lombok.AllArgsConstructor;
import picocli.CommandLine;

@AllArgsConstructor
@CommandLine.Command(
  name = "kernel",
  subcommands = {StartKernelCommand.class, KernelStopCommand.class}
)
public class KernelCommandSet {
  final LauncherContext context;
}
