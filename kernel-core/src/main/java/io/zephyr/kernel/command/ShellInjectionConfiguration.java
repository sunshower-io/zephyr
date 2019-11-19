package io.zephyr.kernel.command;

import dagger.BindsInstance;
import dagger.Component;
import io.zephyr.api.CommandContext;

import javax.inject.Singleton;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

@Singleton
@Component(modules = ShellModule.class)
public interface ShellInjectionConfiguration {
  Shell createShell();

  @Component.Factory
  interface Builder {
    ShellInjectionConfiguration create(
        @BindsInstance ClassLoader classloader,
        @BindsInstance CommandContext context,
        @BindsInstance InputStream inputStream,
        @BindsInstance PrintStream outputWriter);

    default ShellInjectionConfiguration create(
        ClassLoader classloader, CommandContext context, PrintStream outputWriter) {
      return create(classloader, context, System.in, outputWriter);
    }

    default ShellInjectionConfiguration create(ClassLoader classLoader, CommandContext context) {
      return create(classLoader, context, System.out);
    }
  }
}
