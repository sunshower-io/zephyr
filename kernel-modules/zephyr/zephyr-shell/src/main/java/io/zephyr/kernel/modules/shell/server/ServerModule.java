package io.zephyr.kernel.modules.shell.server;

import dagger.Module;
import dagger.Provides;
import io.zephyr.kernel.modules.shell.ShellOptions;
import io.zephyr.kernel.modules.shell.console.Invoker;

@Module
public class ServerModule {

  @Provides
  public Server server(ShellOptions options, Invoker invoker) {
    return new ZephyrServer(options, invoker);
  }
}
