package io.zephyr.kernel.shell;

import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.launch.KernelOptions;
import java.util.*;

import lombok.val;
import picocli.CommandLine;

public class DelegatingShellParser implements ShellParser {

  private final ShellModule module;
  private final ShellConsole console;
  private final CommandRegistry registry;
  private final CommandDelegate delegate;

  public DelegatingShellParser(Kernel kernel, KernelOptions options, CommandRegistry registry) {
    this.delegate = new CommandDelegate();
    this.registry = registry;
    this.console = ServiceLoader.load(ShellConsole.class).findFirst().get();
    this.module = DaggerShellModule.factory().create(kernel, options, console);
  }

  @Override
  public boolean perform(KernelOptions options, String[] rest) {
    val populated = CommandLine.populateCommand(delegate, rest);
    return populated.execute(registry, module);
  }
}
