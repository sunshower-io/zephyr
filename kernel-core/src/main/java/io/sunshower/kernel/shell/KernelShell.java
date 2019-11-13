package io.sunshower.kernel.shell;

import io.sunshower.kernel.core.Kernel;
import io.sunshower.kernel.launch.KernelLauncher;
import io.sunshower.kernel.launch.KernelOptions;

import java.util.regex.Pattern;

@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class KernelShell {

  static final Pattern lineSplitter = Pattern.compile("\\s+");
  private final ShellParser parser;
  private final ShellConsole console;
  private final KernelOptions options;

  public KernelShell(ShellParser parser, ShellConsole console, KernelOptions options) {
    this.parser = parser;
    this.console = console;
    this.options = options;
    KernelLauncher.setConsole(console);
  }

  public void start() {
    boolean running = true;
    String[] line = options.getParameters();

    while (running) {
      if (line == null) {
        line = lineSplitter.split(console.readLine());
      }
      try {
        parser.perform(options, line);
      } catch (Exception ex) {
        console.format("Command not understood--run <help> for a list of valid commands\n");
      }
      if (!options.isInteractive()) {
        return;
      }
      line = null;
    }
  }
}
