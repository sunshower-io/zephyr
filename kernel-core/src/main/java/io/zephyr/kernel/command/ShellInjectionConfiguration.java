package io.zephyr.kernel.command;

import dagger.BindsInstance;
import dagger.Component;
import io.zephyr.api.CommandContext;
import io.zephyr.api.Console;

import java.io.InputStream;
import java.io.PrintStream;
import javax.inject.Singleton;

@Singleton
@Component(modules = ShellModule.class)
public interface ShellInjectionConfiguration {
  Shell createShell();

  @Component.Factory
  interface Builder {
    ShellInjectionConfiguration create(
        @BindsInstance ClassLoader classloader,
        @BindsInstance CommandContext context,
        @BindsInstance Console console);

    default ShellInjectionConfiguration create(
        ClassLoader classLoader,
        CommandContext context,
        InputStream inputStream,
        PrintStream printStream) {
      return create(classLoader, context, new ColoredConsole(inputStream, printStream));
    }

    default ShellInjectionConfiguration create(
        ClassLoader classloader, CommandContext context, PrintStream outputWriter) {
      return create(classloader, context, System.in, outputWriter);
    }

    default ShellInjectionConfiguration create(ClassLoader classLoader, CommandContext context) {
      return create(classLoader, context, System.out);
    }
  }
}
