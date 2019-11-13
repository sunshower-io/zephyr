package io.sunshower.kernel.shell;

import io.sunshower.kernel.launch.KernelOptions;
import lombok.AllArgsConstructor;

import java.util.regex.Pattern;

@AllArgsConstructor
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class KernelShell {

  static final  Pattern       lineSplitter = Pattern.compile("\\s+");
  private final ShellParser   parser;
  private final ShellConsole  console;
  private final KernelOptions options;

  public void start() {
    boolean running = true;
    String[] line = options.getParameters();
    while (running) {
      try {
        parser.perform(options, line);
      } catch (ShellExitException ex) {
        running = false;
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
