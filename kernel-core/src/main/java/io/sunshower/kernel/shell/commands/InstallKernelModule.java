package io.sunshower.kernel.shell.commands;

import java.util.List;
import picocli.CommandLine;

@CommandLine.Command(name = "kernel-module")
@SuppressWarnings({"PMD.DoNotUseThreads", "PMD.DataflowAnomalyAnalysis"})
public class InstallKernelModule implements Runnable {

  @CommandLine.Parameters private List<String> modules;
  @CommandLine.ParentCommand private InstallCommand commandSet;

  @Override
  public void run() {
  }
}
