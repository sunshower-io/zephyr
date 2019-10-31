package io.sunshower.kernel.shell;

import io.sunshower.kernel.launch.KernelOptions;
import java.util.*;
import java.util.regex.Pattern;
import lombok.val;
import picocli.CommandLine;

public class DelegatingShellParser implements ShellParser {

  static final Pattern lineSplitter = Pattern.compile("\\s+");
  private final ShellConsole console;
  private final CommandDelegate delegate;
  private final Map<String, Object> commands;

  public DelegatingShellParser() {
    console = ServiceLoader.load(ShellConsole.class).findFirst().get();
    delegate = new CommandDelegate();
    commands = new HashMap<>();
  }

  @Override
  public boolean perform(KernelOptions options) {
    val line = console.readLine();
    val populated = CommandLine.populateCommand(delegate, lineSplitter.split(line));
    return populated.execute(this);
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
