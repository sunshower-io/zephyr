package io.zephyr.kernel.command;

import io.zephyr.api.Command;
import io.zephyr.api.CommandRegistry;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultCommandRegistry implements CommandRegistry, Serializable {

  private static final long serialVersionUID = 6629591592483320896L;
  private final Map<String, Command> commands;

  public DefaultCommandRegistry() {
    commands = new HashMap<>(10, 2);
  }

  @Override
  public List<Command> getCommands() {
    return new ArrayList<>(commands.values());
  }

  @Override
  public Command register(Command command) {
    return commands.put(command.getName(), command);
  }

  @Override
  public Command unregister(String name) {
    return commands.remove(name);
  }

  @Override
  public Command resolve(String name) {
    return commands.get(name);
  }
}
