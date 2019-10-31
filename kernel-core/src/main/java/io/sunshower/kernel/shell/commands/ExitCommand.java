package io.sunshower.kernel.shell.commands;

import io.sunshower.kernel.shell.ShellExitException;
import java.util.concurrent.Callable;
import picocli.CommandLine;

@CommandLine.Command(name = "exit")
public class ExitCommand implements Callable<Void> {
  @Override
  public Void call() throws Exception {
    throw new ShellExitException();
  }
}
