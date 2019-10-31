package io.sunshower.kernel.shell.commands;

import java.util.concurrent.Callable;
import picocli.CommandLine;

@CommandLine.Command(name = "exit")
public class ExitCommand implements Callable<Void> {
  @Override
  public Void call() throws Exception {
    System.exit(0);
    return null;
  }
}
