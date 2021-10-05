package io.zephyr.support.flyway;

import io.zephyr.kernel.Module;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipFile;
import lombok.val;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.resource.LoadableResource;

public class ClasspathModuleResourceProvider implements ResourceProvider {

  private final Module module;
  private final List<String> locations;
  private final Map<String, LoadableResource> resources;

  public ClasspathModuleResourceProvider(Module module, String... locations) {
    this.module = Objects.requireNonNull(module, "Module must not be null");
    validate(locations);
    this.locations = Arrays.asList(locations);
    this.resources = new LinkedHashMap<>();
    loadResources();
  }

  @Override
  public LoadableResource getResource(String name) {
    return resources.get(name);
  }

  @Override
  public Collection<LoadableResource> getResources(String prefix, String[] suffixes) {
    return Collections.unmodifiableCollection(resources.values());
  }

  private void validate(String[] locations) {
    if (locations.length == 0) {
      throw new IllegalArgumentException("Error: locations must not be empty");
    }
  }

  private void loadResources() {
    try (val file = new ZipFile(module.getAssembly().getFile())) {
      for (val location : locations) {
        var normalizedLocation = location;
        if (isWar(file)) {
          normalizedLocation = "WEB-INF/classes/" + location;
        }
        val entries = file.entries();
        while (entries.hasMoreElements()) {
          val next = entries.nextElement();
          if (!next.isDirectory()
              && next.getName().startsWith(normalizedLocation)
              && next.getName().endsWith(".sql")) {
            resources.put(
                next.getName(),
                new ModuleLoadableResource(module.getAssembly().getFile(), file, next, location));
          }
        }
      }

    } catch (IOException ex) {
      throw new FlywayException(ex);
    }
  }

  private boolean isWar(ZipFile file) {
    return file.getEntry("WEB-INF/classes") != null;
  }
}
