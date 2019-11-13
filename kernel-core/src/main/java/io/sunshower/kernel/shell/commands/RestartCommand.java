package io.sunshower.kernel.shell.commands;

import picocli.CommandLine;

@CommandLine.Command(name = "restart")
public class RestartCommand implements Runnable {
  @Override
  public void run() {
    throw new RestartException();
  }
}
