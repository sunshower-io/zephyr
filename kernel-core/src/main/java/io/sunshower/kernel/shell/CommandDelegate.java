package io.sunshower.kernel.shell;

import io.sunshower.kernel.launch.KernelLauncher;
import io.sunshower.kernel.misc.SuppressFBWarnings;
import lombok.Getter;
import lombok.val;
import picocli.CommandLine;

import java.util.Comparator;

@Getter
@SuppressFBWarnings
@CommandLine.Command
public class CommandDelegate {
  @CommandLine.Parameters(index = "0")
  private String command;

  @CommandLine.Parameters(index = "1..*")
  private String[] arguments;

  public boolean execute(CommandRegistry registry) {
    checkHelp(registry);
    val cli = registry.getCommand(command);
    if (cli == null) {
      return false;
    }
    val commandLine =
        new CommandLine(cli).setExecutionExceptionHandler(KernelLauncher.getInstance());
    if (arguments == null || arguments.length == 0) {
      commandLine.execute();
    } else {
      commandLine.execute(arguments);
    }
    arguments = null;
    return true;
  }

  private void checkHelp(CommandRegistry registry) {
    if (command != null && command.trim().toLowerCase().equals("help")) {
      for (val command : registry.getCommands()) {
        val help = new CommandLine.Help(command.snd);
        System.out.println("Command: " + help.abbreviatedSynopsis());
        val result = help.commandList();
        System.out.println(result);
      }
    }
  }
}
