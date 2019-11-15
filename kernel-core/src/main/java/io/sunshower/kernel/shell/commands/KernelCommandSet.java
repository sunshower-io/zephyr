package io.sunshower.kernel.shell.commands;

import io.sunshower.kernel.shell.Command;
import picocli.CommandLine;

@CommandLine.Command(
  name = "kernel",
  subcommands = {StartKernelCommand.class, KernelStopCommand.class, RestartCommand.class}
)
public class KernelCommandSet extends Command {}
