package io.zephyr.kernel.server;

import dagger.Component;

@Component(modules = ServerModule.class)
public interface ServerInjectionConfiguration {

  Server server();

  @Component.Factory
  interface Builder {
    ServerInjectionConfiguration build();
  }
}
