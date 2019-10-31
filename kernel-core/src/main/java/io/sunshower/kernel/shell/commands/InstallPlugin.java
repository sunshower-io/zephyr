package io.sunshower.kernel.shell.commands;

import picocli.CommandLine;

@CommandLine.Command(name = "plugin")
public class InstallPlugin implements Runnable {
  @CommandLine.ParentCommand InstallCommand commandSet;

  @Override
  public void run() {
    System.out.println("install plugin");
    System.out.println(commandSet);
  }
}
