package io.zephyr.kernel.shell.commands;

import io.zephyr.kernel.shell.Command;
import picocli.CommandLine;

@CommandLine.Command(
  name = "kernel",
  subcommands = {StartKernelCommand.class, KernelStopCommand.class, RestartCommand.class}
)
public class KernelCommandSet extends Command {}
