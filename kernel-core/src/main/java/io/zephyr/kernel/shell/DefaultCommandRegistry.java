package io.zephyr.kernel.shell;

import io.sunshower.gyre.Pair;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.NonNull;

public class DefaultCommandRegistry implements CommandRegistry {

  private final Map<String, Command> commands;

  public DefaultCommandRegistry() {
    commands = new HashMap<>();
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
  public void register(@NonNull String name, @NonNull Command command) {
    commands.put(name, command);
  }

  @Override
  public Command getCommand(@NonNull String command) {
    return commands.get(command);
  }
}
