package io.zephyr.kernel.server;

import dagger.BindsInstance;
import dagger.Component;
import io.zephyr.api.Console;
import io.zephyr.api.Invoker;
import io.zephyr.kernel.launch.KernelOptions;
import io.zephyr.kernel.launch.RecordingConsole;

@Component(modules = ServerModule.class)
public interface ServerInjectionConfiguration {

  Server server();

  @Component.Factory
  interface Builder {
    ServerInjectionConfiguration build(
        @BindsInstance KernelOptions options,
        @BindsInstance Invoker invoker,
        @BindsInstance Console console);
  }
}
