package io.zephyr.kernel.command;

import io.zephyr.api.CommandRegistry;
import io.zephyr.kernel.launch.KernelLauncher;
import io.zephyr.kernel.misc.SuppressFBWarnings;
import lombok.AllArgsConstructor;
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

  final CommandRegistry registry;

  public CommandDelegate(final CommandRegistry registry) {
    this.registry = registry;
  }

  @SuppressWarnings("PMD.NullAssignment")
  public boolean execute() {
    val cli = registry.resolve(command);

    if (cli == null) {
      return false;
    }
    val commandLine =
        new CommandLine(cli, injectionFactory())
            .setExecutionExceptionHandler(KernelLauncher.getInstance());
    if (arguments == null || arguments.length == 0) {
      commandLine.execute();
    } else {
      commandLine.execute(arguments);
    }
    arguments = null;
    return true;
  }

  private CommandLine.IFactory injectionFactory() {
    return new InjectionFactory();
  }

  @AllArgsConstructor
  static final class InjectionFactory implements CommandLine.IFactory {

    @Override
    @SuppressWarnings("unchecked")
    public <K> K create(Class<K> cls) throws Exception {
      return CommandLine.defaultFactory().create(cls);
    }
  }
}
