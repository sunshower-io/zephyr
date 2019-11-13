package io.sunshower.kernel.shell;

import io.sunshower.gyre.Pair;
import io.sunshower.kernel.launch.KernelOptions;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import lombok.val;
import picocli.CommandLine;

public class DelegatingShellParser implements ShellParser {

  private final ShellConsole console;
  private final CommandDelegate delegate;
  private final Map<String, Object> commands;

  public DelegatingShellParser() {
    console = ServiceLoader.load(ShellConsole.class).findFirst().get();
    delegate = new CommandDelegate();
    commands = new HashMap<>();
  }

  @Override
  public boolean perform(KernelOptions options, String[] rest) {
    val populated = CommandLine.populateCommand(delegate, rest);
    return populated.execute(this);
  }

  @Override
  public List<Pair<String, Object>> getCommands() {
    return commands.entrySet().stream()
        .map(t -> Pair.of(t.getKey(), t.getValue()))
        .collect(Collectors.toList());
  }

  @Override
  public void register(String name, Object command) {
    commands.put(name, command);
  }

  @Override
  public Object getCommand(String command) {
    return commands.get(command);
  }
}
