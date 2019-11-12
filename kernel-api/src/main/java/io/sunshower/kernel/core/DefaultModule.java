package io.sunshower.kernel.core;

import io.sunshower.PluginActivator;
import io.sunshower.kernel.*;
import io.sunshower.kernel.Module;
import io.sunshower.kernel.misc.SuppressFBWarnings;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.ServiceLoader;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;

public class DefaultModule implements Module, Comparable<Module> {

  /**
   * mutable state . These can't be final because either the module isn't resolved or there is a
   * mutual dependency
   */
  @Setter private ModuleLoader moduleLoader;

  @Setter @Getter private PluginActivator activator;

  @Getter @Setter private Lifecycle lifecycle;
  @Setter private ModuleClasspath moduleClasspath;

  /** immutable state */
  @Getter private final int order;

  @Getter private final Type type;

  @Getter private final Source source;
  @Getter private final Assembly assembly;
  @Getter private final Path moduleDirectory;
  @Getter private final Coordinate coordinate;
  @Getter private final FileSystem fileSystem;

  @Getter private final Set<Library> libraries;
  @Getter private final Set<Dependency> dependencies;

  public DefaultModule(
      int order,
      Type type,
      Source source,
      Assembly assembly,
      Path moduleDirectory,
      Coordinate coordinate,
      FileSystem fileSystem,
      Set<Library> libraries,
      Set<Dependency> dependencies) {
    this.order = order;
    this.type = type;
    this.source = source;
    this.assembly = assembly;
    this.libraries = libraries;
    this.coordinate = coordinate;
    this.fileSystem = fileSystem;
    this.dependencies = dependencies;
    this.moduleDirectory = moduleDirectory;
  }

  public DefaultModule(
      Type type,
      Source source,
      Assembly assembly,
      Path moduleDirectory,
      Coordinate coordinate,
      FileSystem fileSystem,
      Set<Library> libraries,
      Set<Dependency> dependencies) {
    this(
        10,
        type,
        source,
        assembly,
        moduleDirectory,
        coordinate,
        fileSystem,
        libraries,
        dependencies);
  }

  @SneakyThrows
  public ModuleClasspath getModuleClasspath() {
    if (moduleClasspath == null) {
      if (moduleLoader == null) {
        throw new IllegalStateException("ModuleLoader must not be null");
      }

      moduleClasspath = moduleLoader.loadModule(coordinate);
    }
    return moduleClasspath;
  }

  @Override
  @SuppressFBWarnings
  public ClassLoader getClassLoader() {
    if (moduleClasspath == null) {
      getModuleClasspath();
      if (moduleClasspath == null) {
        throw new IllegalModuleStateException(
            "Module must be in at least the 'RESOLVED' state to perform this operation");
      }
    }
    return moduleClasspath.getClassLoader();
  }

  @Override
  public boolean dependsOn(Coordinate m, Transitivity transitivity) {
    return false;
  }

  @Override
  public <S> ServiceLoader<S> resolveServiceLoader(Class<S> type) {
    return moduleClasspath.resolveServiceLoader(type);
  }

  @Override
  public int compareTo(io.sunshower.kernel.Module o) {
    return coordinate.compareTo(o.getCoordinate());
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o == null) {
      return false;
    }

    if (o instanceof io.sunshower.kernel.Module) {
      val other = (io.sunshower.kernel.Module) o;
      return coordinate.equals(other.getCoordinate());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return coordinate.hashCode();
  }
}
