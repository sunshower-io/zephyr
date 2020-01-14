package io.zephyr.kernel.modules.shell.command;

import io.zephyr.kernel.modules.shell.console.Command;
import io.zephyr.kernel.modules.shell.console.History;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.val;

public class DefaultHistory implements History {

  private final List<Command> commands;

  public DefaultHistory() {
    commands = new ArrayList<>();
  }

  public void add(Command command) {
    commands.add(command);
  }

  @Override
  public List<Command> clear() {
    return null;
  }

  @Override
  public List<Command> getHistory() {
    return Collections.unmodifiableList(commands);
  }

  @Override
  public List<Command> getHistory(int count) {
    val size = commands.size();
    return commands.subList(Math.max(0, size - count), size);
  }

  @Override
  public List<Command> getHistory(String match) {
    return Collections.emptyList();
  }
}
