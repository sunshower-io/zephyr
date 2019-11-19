package io.zephyr.kernel.launch;

import io.zephyr.kernel.misc.SuppressFBWarnings;
import lombok.val;
import picocli.CommandLine;

@SuppressFBWarnings
@SuppressWarnings({"PMD.UseVarargs", "PMD.ArrayIsStoredDirectly", "PMD.DoNotCallSystemExit"})
public class KernelLauncher {

  final String[] arguments;
  final KernelOptions options;


  public enum Action {
    /**
     * run command server
     */
    Server,

    /**
     * run interactive (command or server)
     */
    Interactive,

    /**
     * Run a command and exit
     */
    Single
  }




  KernelLauncher(final KernelOptions options, final String[] arguments) {
    this.options = options;
    this.arguments = arguments;
  }

  void run() {

  }


  public static void main(String[] args) {
    val options = CommandLine.populateSpec(KernelOptions.class, args);
    new KernelLauncher(options, args).run();
  }
}
