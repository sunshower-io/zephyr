package io.zephyr.kernel.shell;

import io.zephyr.kernel.launch.KernelOptions;

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
  }

  public void start() {
    boolean running = true;
    String[] line = options.getParameters();

    while (running) {
      try {
        parser.perform(options, line);
      } catch (Exception ex) {
        console.format("Command not understood--run <help> for a list of valid commands\n");
      }

      if (!options.isInteractive()) {
        return;
      }
      line = lineSplitter.split(console.readLine());
    }
  }
}
