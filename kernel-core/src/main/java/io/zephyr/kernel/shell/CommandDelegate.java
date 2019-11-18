package io.zephyr.kernel.shell;

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

  @SuppressWarnings("PMD.NullAssignment")
  public boolean execute(CommandRegistry registry, ShellModule module) {
    val cli = registry.getCommand(command);
    module.inject(cli);
    if (cli == null) {
      return false;
    }
    val commandLine =
        new CommandLine(cli, injectionFactory(module))
            .setExecutionExceptionHandler(KernelLauncher.getInstance());
    if (arguments == null || arguments.length == 0) {
      commandLine.execute();
    } else {
      commandLine.execute(arguments);
    }
    arguments = null;
    return true;
  }

  private CommandLine.IFactory injectionFactory(ShellModule module) {
    return new InjectionFactory(module);
  }

  @AllArgsConstructor
  static final class InjectionFactory implements CommandLine.IFactory {

    final ShellModule module;

    @Override
    @SuppressWarnings("unchecked")
    public <K> K create(Class<K> cls) throws Exception {
      if (Command.class.isAssignableFrom(cls)) {
        val cmd = (Command) cls.getConstructor().newInstance();
        module.inject(cmd);
        return (K) cmd;
      } else {
        return CommandLine.defaultFactory().create(cls);
      }
    }
  }
}
