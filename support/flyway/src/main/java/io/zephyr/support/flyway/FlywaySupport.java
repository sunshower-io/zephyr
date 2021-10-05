package io.zephyr.support.flyway;

import io.zephyr.kernel.Module;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.flywaydb.core.api.ResourceProvider;

public class FlywaySupport {

  public static ModuleResourceProviderBuilder classpath(@NonNull Module module) {
    return new ModuleResourceProviderBuilder(module);
  }

  @AllArgsConstructor
  public static class ModuleResourceProviderBuilder {
    final Module module;

    public ResourceProvider locations(String... locations) {
      return new ClasspathModuleResourceProvider(module, locations);
    }
  }
}
