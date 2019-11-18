package io.zephyr.kernel.shell.commands;

import io.zephyr.kernel.misc.SuppressFBWarnings;
import io.zephyr.kernel.shell.Command;
import picocli.CommandLine;

@SuppressFBWarnings
@CommandLine.Command(name = "exit")
@SuppressWarnings({"PMD.DoNotUseThreads", "PMD.DoNotCallSystemExit", "PMD.SystemPrintln"})
public class ExitCommand extends Command {

  @Override
  protected int execute() {
    System.out.println("Exit");
    return 0;
  }
}
