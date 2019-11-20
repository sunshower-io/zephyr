package io.zephyr.kernel.server;

import dagger.Module;
import dagger.Provides;
import io.zephyr.api.Invoker;
import io.zephyr.kernel.launch.KernelOptions;


@Module
public class ServerModule {

  @Provides
  public Server server(KernelOptions options, Invoker invoker) {
    return new ZephyrServer(options, invoker);
  }
}
