package io.zephyr.kernel.command;

import dagger.Module;
import dagger.Provides;
import io.zephyr.api.CommandContext;
import io.zephyr.api.CommandRegistry;
import io.zephyr.api.CommandRegistryDecorator;
import io.zephyr.api.Console;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import javax.inject.Singleton;
import lombok.val;

@Module
public class ShellModule {

  @Provides
  @Singleton
  public Console console(
      InputStream inputStream, PrintStream outputStream, CommandContext context) {
    val result = new ColoredConsole(inputStream, outputStream);
    ((DefaultCommandContext) context).register(Console.class, result);
    return result;
  }

  @Provides
  @Singleton
  public Shell shell(CommandRegistry registry, CommandContext context, Console console) {
    return new Shell(registry, context, console);
  }

  @Provides
  @Singleton
  public CommandRegistry commandRegistry(List<CommandRegistryDecorator> decorators) {
    val results = new DefaultCommandRegistry();
    for (val decorator : decorators) {
      decorator.decorate(results);
    }
    return results;
  }

  @Provides
  public List<CommandRegistryDecorator> commandRegistryDecorators(ClassLoader classloader) {
    val decorators = ServiceLoader.load(CommandRegistryDecorator.class, classloader).iterator();
    val results = new ArrayList<CommandRegistryDecorator>();
    while (decorators.hasNext()) {
      results.add(decorators.next());
    }
    return results;
  }
}
