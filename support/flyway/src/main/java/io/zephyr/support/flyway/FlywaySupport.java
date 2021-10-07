package io.zephyr.support.flyway;

import io.zephyr.kernel.Module;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.flywaydb.core.api.ResourceProvider;

public class FlywaySupport {

  public static ModuleResourceProviderBuilder classpath(@NonNull Module module) {
    return new ModuleResourceProviderBuilder(module, false);
  }

  public static ModuleResourceProviderBuilder classpath(@NonNull Module module,
      boolean searchSubAssemblies) {
    return new ModuleResourceProviderBuilder(module, searchSubAssemblies);
  }

  @AllArgsConstructor
  public static class ModuleResourceProviderBuilder {

    final Module module;
    final boolean searchSubAssemblies;

    public ResourceProvider locations(String... locations) {
      return new ClasspathModuleResourceProvider(module, searchSubAssemblies, locations);
    }
  }
}
