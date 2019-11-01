package io.sunshower.kernel.shell.commands;

import picocli.CommandLine;

@SuppressWarnings("PMD.DoNotUseThreads")
@CommandLine.Command(name = "plugin")
public class InstallPlugin implements Runnable {
  @CommandLine.ParentCommand InstallCommand commandSet;

  @Override
  public void run() {}
}
