package io.zephyr.kernel.server;

import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
public class ServerModule {

  @Provides
  public Server server() {
    return new ZephyrServer();
  }
}
