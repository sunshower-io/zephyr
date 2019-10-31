package io.sunshower.kernel.launch;

import dagger.Module;
import dagger.Provides;
import io.sunshower.kernel.shell.*;
import java.util.ServiceLoader;
import javax.inject.Singleton;
import lombok.NonNull;
import lombok.val;

@Module
public class LauncherInjectionConfiguration {

  private final KernelOptions options;

  public LauncherInjectionConfiguration(@NonNull KernelOptions options) {
    this.options = options;
  }

  @Provides
  @Singleton
  public ShellConsole shellConsole() {
    return ServiceLoader.load(ShellConsole.class).findFirst().get();
  }

  @Provides
  @Singleton
  public ShellParser shellParser() {
    return ServiceLoader.load(ShellParser.class).findFirst().get();
  }

  @Provides
  @Singleton
  public KernelShell shell(ShellParser parser, LauncherContext context, ShellConsole console) {
    return new KernelShell(parser, console, options);
  }

  @Provides
  @Singleton
  public KernelOptions kernelOptions() {
    return options;
  }

  @Provides
  @Singleton
  public LauncherContext context(ShellParser registry, ShellConsole console) {
    val ctx = new KernelLauncherContext(console, registry, options);
    val loader = ServiceLoader.load(LauncherDecorator.class);
    val iter = loader.iterator();
    while (iter.hasNext()) {
      iter.next().decorate(ctx);
    }
    return ctx;
  }
}
