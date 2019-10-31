package io.sunshower.kernel.shell;

import io.sunshower.kernel.launch.KernelOptions;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class KernelShell {

  private final ShellParser parser;
  private final ShellConsole console;
  private final KernelOptions options;

  public void start() {
    for (; ; ) {
      try {
        parser.perform(options);
      } catch (ShellExitException ex) {
        console.format("Goodbye");
        return;
      } catch (Exception ex) {
        console.format("Command not understood--run <help> for a list of valid commands\n");
      }
      if (!options.isInteractive()) {
        return;
      }
    }
  }
}
