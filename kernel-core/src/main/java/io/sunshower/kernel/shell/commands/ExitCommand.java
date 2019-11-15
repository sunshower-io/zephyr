package io.sunshower.kernel.shell.commands;

import io.sunshower.kernel.events.Event;
import io.sunshower.kernel.events.EventType;
import io.sunshower.kernel.misc.SuppressFBWarnings;
import io.sunshower.kernel.shell.Command;
import picocli.CommandLine;

@SuppressFBWarnings
@CommandLine.Command(name = "exit")
@SuppressWarnings({"PMD.DoNotUseThreads", "PMD.DoNotCallSystemExit"})
public class ExitCommand extends Command {

  @Override
  protected int execute() {
    System.out.println("Exit");
    return 0;
  }


}
