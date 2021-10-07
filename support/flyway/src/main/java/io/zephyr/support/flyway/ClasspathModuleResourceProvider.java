package io.zephyr.support.flyway;

import io.zephyr.kernel.Module;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import lombok.extern.java.Log;
import lombok.val;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.resource.LoadableResource;

@Log
public class ClasspathModuleResourceProvider implements ResourceProvider {

  private final Module module;
  private final List<String> locations;
  private final boolean searchSubAssemblies;
  private final Map<String, LoadableResource> resources;

  public ClasspathModuleResourceProvider(Module module, String... locations) {
    this(module, false, locations);
  }

  public ClasspathModuleResourceProvider(Module module, boolean searchSubAssemblies,
      String... locations) {
    this.module = Objects.requireNonNull(module, "Module must not be null");
    validate(locations);
    this.locations = Arrays.asList(locations);
    this.resources = new LinkedHashMap<>();
    this.searchSubAssemblies = searchSubAssemblies;
  }


  @Override
  public LoadableResource getResource(String name) {
    return resources.get(name);
  }

  @Override
  public Collection<LoadableResource> getResources(String prefix, String[] suffixes) {
    resources.clear();
    loadResourcesIn(module.getAssembly().getFile(), prefix, suffixes);
    if (searchSubAssemblies) {
      for (val library : module.getAssembly().getLibraries()) {
        loadResourcesIn(library.getFile(), prefix, suffixes);
      }
    }
    return Collections.unmodifiableCollection(resources.values());
  }

  private void loadResourcesIn(File file, String prefix, String[] suffixes) {
    try {
      loadResources(file, prefix, suffixes);
    } catch (ZipException ex) {
      log.log(Level.WARNING, "Error opening assembly file: ''{0}''", ex.getMessage());
    }
  }

  private void validate(String[] locations) {
    if (locations.length == 0) {
      throw new IllegalArgumentException("Error: locations must not be empty");
    }
  }

  private void loadResources(File assemblyFile, String prefix, String[] suffixes)
      throws ZipException {
    try (val file = new ZipFile(assemblyFile)) {
      for (val location : locations) {
        var normalizedLocation = location;
        if (isWar(file)) {
          normalizedLocation = "WEB-INF/classes/" + location;
        }
        val entries = file.entries();
        while (entries.hasMoreElements()) {
          val next = entries.nextElement();
          val nextSegs = next.getName().split(Pattern.quote("/"));
          val nextName = nextSegs[nextSegs.length - 1];
          if (!next.isDirectory()
              && next.getName().startsWith(normalizedLocation)
              && nextName.startsWith(prefix)
              && endsWith(nextName, suffixes)) {
            resources.put(
                next.getName(),
                new ModuleLoadableResource(module.getAssembly().getFile(), file, next, location));
          }
        }
      }
    } catch (ZipException ex) {
      throw ex;
    } catch (IOException ex) {
      throw new FlywayException(ex);
    }
  }

  private boolean endsWith(String next, String[] suffixes) {
    for (val suffix : suffixes) {
      if (next.endsWith(suffix)) {
        return true;
      }
    }
    return false;
  }

  private boolean isWar(ZipFile file) {
    return file.getEntry("WEB-INF/classes") != null;
  }
}
