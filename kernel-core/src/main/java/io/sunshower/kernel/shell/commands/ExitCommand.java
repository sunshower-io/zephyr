package io.sunshower.kernel.shell.commands;

import io.sunshower.kernel.misc.SuppressFBWarnings;
import java.util.concurrent.Callable;

import io.sunshower.kernel.shell.ShellExitException;
import picocli.CommandLine;

@SuppressFBWarnings
@CommandLine.Command(name = "exit")
@SuppressWarnings({"PMD.DoNotUseThreads", "PMD.DoNotCallSystemExit"})
public class ExitCommand implements Callable<Void> {
  @Override
  public Void call() throws Exception {
    throw new ShellExitException();
  }
}
