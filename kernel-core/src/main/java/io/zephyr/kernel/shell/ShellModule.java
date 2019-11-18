package io.zephyr.kernel.shell;

import dagger.BindsInstance;
import dagger.Component;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.launch.KernelOptions;
import javax.inject.Singleton;

@Singleton
@Component(modules = ShellInjectionConfiguration.class)
public interface ShellModule {

  void inject(Command command);

  @Component.Factory
  interface Builder {
    ShellModule create(
        @BindsInstance Kernel kernel,
        @BindsInstance KernelOptions options,
        @BindsInstance ShellConsole console);
  }
}
