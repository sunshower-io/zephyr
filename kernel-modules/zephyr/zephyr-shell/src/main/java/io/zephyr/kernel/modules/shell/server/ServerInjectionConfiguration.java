package io.zephyr.kernel.modules.shell.server;

import dagger.BindsInstance;
import dagger.Component;
import io.zephyr.kernel.modules.shell.ShellOptions;
import io.zephyr.kernel.modules.shell.command.ColoredConsole;
import io.zephyr.kernel.modules.shell.console.Console;
import io.zephyr.kernel.modules.shell.console.Invoker;

@Component(modules = ServerModule.class)
public interface ServerInjectionConfiguration {

  Server server();

  @Component.Factory
  interface Builder {

    default ServerInjectionConfiguration build(ShellOptions options, Invoker invoker) {
      return build(options, invoker, new ColoredConsole());
    }

    ServerInjectionConfiguration build(
        @BindsInstance ShellOptions options,
        @BindsInstance Invoker invoker,
        @BindsInstance Console console);
  }
}
