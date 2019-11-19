package io.zephyr.kernel.command;

import io.zephyr.api.*;
import lombok.NonNull;
import lombok.val;

public abstract class Shell implements Invoker {
  protected final Console console;

  private final DefaultHistory history;
  private final CommandContext context;
  private final CommandRegistry registry;

  protected Shell(
      @NonNull CommandRegistry registry,
      @NonNull CommandContext context,
      @NonNull Console console) {
    this.console = console;
    this.registry = registry;
    this.context = context;
    this.history = new DefaultHistory();
  }

  @Override
  public Result invoke(String commandName, Parameters parameters) {
    val command = registry.resolve(commandName);
    if (command == null) {
      throw new CommandNotFoundException(commandName);
    }

    history.add(command);
    return command.invoke(context, parameters);
  }

  @Override
  public final CommandRegistry getRegistry() {
    return registry;
  }

  @Override
  public final History getHistory() {
    return history;
  }
}
