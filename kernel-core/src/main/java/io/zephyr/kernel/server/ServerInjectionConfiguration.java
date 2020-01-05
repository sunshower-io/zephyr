package io.zephyr.kernel.server;

import dagger.BindsInstance;
import dagger.Component;
import io.zephyr.cli.Console;
import io.zephyr.cli.Invoker;
import io.zephyr.kernel.command.ColoredConsole;
import io.zephyr.kernel.launch.KernelOptions;

@Component(modules = ServerModule.class)
public interface ServerInjectionConfiguration {

  Server server();

  @Component.Factory
  interface Builder {

    default ServerInjectionConfiguration build(KernelOptions options, Invoker invoker) {
      return build(options, invoker, new ColoredConsole());
    }

    ServerInjectionConfiguration build(
        @BindsInstance KernelOptions options,
        @BindsInstance Invoker invoker,
        @BindsInstance Console console);
  }
}
