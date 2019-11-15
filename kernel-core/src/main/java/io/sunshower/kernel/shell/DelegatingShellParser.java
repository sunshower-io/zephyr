package io.sunshower.kernel.shell;

import io.sunshower.gyre.Pair;
import io.sunshower.kernel.core.Kernel;
import io.sunshower.kernel.launch.KernelOptions;
import java.util.*;
import java.util.stream.Collectors;
import lombok.val;
import picocli.CommandLine;

public class DelegatingShellParser implements ShellParser {

  private final ShellModule module;
  private final ShellConsole console;
  private final CommandDelegate delegate;
  private final Map<String, Command> commands;

  public DelegatingShellParser(Kernel kernel, KernelOptions options) {
    console = ServiceLoader.load(ShellConsole.class).findFirst().get();
    delegate = new CommandDelegate();
    commands = new HashMap<>();
    module = DaggerShellModule.factory().create(kernel, options, console);
  }

  @Override
  public boolean perform(KernelOptions options, String[] rest) {
    val populated = CommandLine.populateCommand(delegate, rest);
    return populated.execute(this, module);
  }

  @Override
  public List<Pair<String, Command>> getCommands() {
    return commands
        .entrySet()
        .stream()
        .map(t -> Pair.of(t.getKey(), t.getValue()))
        .collect(Collectors.toList());
  }

  @Override
  public void register(String name, Command command) {
    commands.put(name, command);
  }

  @Override
  public Command getCommand(String command) {
    return commands.get(command);
  }
}
