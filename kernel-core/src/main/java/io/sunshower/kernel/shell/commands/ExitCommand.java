package io.sunshower.kernel.shell.commands;

import io.sunshower.kernel.misc.SuppressFBWarnings;
import java.util.concurrent.Callable;
import picocli.CommandLine;

@SuppressFBWarnings
@CommandLine.Command(name = "exit")
@SuppressWarnings({"PMD.DoNotUseThreads", "PMD.DoNotCallSystemExit"})
public class ExitCommand implements Callable<Void> {
  @Override
  public Void call() throws Exception {
    System.exit(0);
    return null;
  }
}
