package io.sunshower.kernel.shell;

import io.sunshower.kernel.misc.SuppressFBWarnings;
import lombok.Getter;
import lombok.val;
import picocli.CommandLine;

@Getter
@SuppressFBWarnings
@CommandLine.Command
public class CommandDelegate {
  @CommandLine.Parameters(index = "0")
  private String command;

  @CommandLine.Parameters(index = "1..*")
  private String[] arguments;

  public boolean execute(CommandRegistry registry) {
    val cli = registry.getCommand(command);
    val commandLine = new CommandLine(cli);
    if (arguments == null || arguments.length == 0) {
      commandLine.execute();
    } else {
      commandLine.execute(arguments);
    }
    return true;
  }
}
