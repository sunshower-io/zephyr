package io.zephyr.kernel.shell.commands;

import io.zephyr.kernel.shell.Color;
import io.zephyr.kernel.shell.Command;
import lombok.val;
import picocli.CommandLine;

@CommandLine.Command(name = "list")
public class ListPluginCommand extends Command {

  @Override
  protected int execute() {
    console.writeln("Installed Plugins:\n", Color.colors(Color.Green));
    val modules = kernel.getModuleManager().getModules();
    for (val module : modules) {
      console.writeln(
          "\t%s | state: %s\n",
          Color.colors(Color.Green),
          module.getCoordinate().toCanonicalForm(),
          module.getLifecycle().getState());
    }

    return 0;
  }
}
