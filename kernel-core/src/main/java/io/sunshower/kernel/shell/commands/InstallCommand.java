package io.sunshower.kernel.shell.commands;

import io.sunshower.kernel.shell.LauncherContext;
import lombok.AllArgsConstructor;
import picocli.CommandLine;

@AllArgsConstructor
@CommandLine.Command(
  name = "install",
  subcommands = {InstallKernelModule.class, InstallPlugin.class}
)
@SuppressWarnings("PMD.DoNotUseThreads")
public class InstallCommand {
  final LauncherContext context;
}
