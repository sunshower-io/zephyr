package io.zephyr.kernel.command;

import io.zephyr.api.*;
import lombok.NonNull;
import lombok.val;
import picocli.CommandLine;

import java.rmi.Remote;
import java.rmi.RemoteException;

public abstract class Shell implements Invoker, Remote {
  protected final transient Console console;

  private final transient DefaultHistory history;
  private final transient CommandContext context;
  private final transient CommandRegistry registry;

  final CommandDelegate delegate;

  protected Shell(
      @NonNull CommandRegistry registry,
      @NonNull CommandContext context,
      @NonNull Console console) {
    this.console = console;
    this.registry = registry;
    this.context = context;
    this.history = new DefaultHistory();
    this.delegate = new CommandDelegate(registry, history, context);
  }

  @Override
  public Result invoke(Parameters parameters) throws RemoteException {

    try {
      val cli = new CommandLine(delegate).setUnmatchedArgumentsAllowed(true);
      cli.execute(parameters.formals());

      //      val a = CommandLine.populateCommand(delegate, parameters.formals());
      //      a.execute(context);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
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
