package io.zephyr.kernel.command;

import io.zephyr.api.CommandContext;
import io.zephyr.api.CommandRegistry;
import io.zephyr.api.Console;
import lombok.NonNull;
import lombok.val;
import picocli.CommandLine;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Scanner;

/**
 * LocalShell must run in the same process-space as the actual kernel (i.e. is local to the kernel).
 * This can be used to safely modify a running kernel instance
 */
public class LocalShell extends Shell {

  private volatile boolean running;
  private final CommandDelegate delegate;

  protected LocalShell(
      @NonNull CommandRegistry registry,
      @NonNull CommandContext context,
      @NonNull Console console) {
    super(registry, context, console);
    delegate = new CommandDelegate(registry, (DefaultHistory) getHistory(), context);
  }

  @Override
  public void start() throws Exception {
  }

  @Override
  public void stop() throws Exception {
    running = false;
  }
}
