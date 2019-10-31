package io.sunshower.kernel.launch;

import dagger.Component;
import io.sunshower.kernel.shell.KernelShell;
import io.sunshower.kernel.shell.LauncherContext;
import javax.inject.Singleton;

@Singleton
@Component(modules = LauncherInjectionConfiguration.class)
public interface LauncherInjectionModule {

  KernelShell shell();

  LauncherContext launcherContext();
}
