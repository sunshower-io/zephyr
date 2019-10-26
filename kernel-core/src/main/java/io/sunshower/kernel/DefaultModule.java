package io.sunshower.kernel;

import io.sunshower.kernel.misc.SuppressFBWarnings;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.ServiceLoader;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleLoader;
import org.jetbrains.annotations.NotNull;

public class DefaultModule
    implements io.sunshower.kernel.Module, Comparable<io.sunshower.kernel.Module> {

  /**
   * mutable state . These can't be final because either the module isn't resolved or there is a
   * mutual dependency
   */
  @Setter private Module module;

  @Setter private ModuleLoader loader;

  @Getter @Setter private Lifecycle lifecycle;

  @Getter private final Type type;

  @Getter private final Source source;
  @Getter private final Assembly assembly;
  @Getter private final Path moduleDirectory;
  @Getter private final Coordinate coordinate;
  @Getter private final FileSystem fileSystem;

  // must be unmodifiable upon construction
  @Getter private final Set<Library> libraries;
  @Getter private final Set<Dependency> dependencies;

  public DefaultModule(
      Type type,
      Source source,
      Assembly assembly,
      Path moduleDirectory,
      Coordinate coordinate,
      FileSystem fileSystem,
      Set<Library> libraries,
      Set<Dependency> dependencies) {
    this.type = type;
    this.source = source;
    this.assembly = assembly;
    this.libraries = libraries;
    this.coordinate = coordinate;
    this.fileSystem = fileSystem;
    this.dependencies = dependencies;
    this.moduleDirectory = moduleDirectory;
  }

  @SneakyThrows
  public Module getModule() {
    if (module == null) {
      if (loader == null) {
        throw new IllegalStateException("ModuleLoader must not be null");
      }

      module = loader.loadModule(coordinate.toCanonicalForm());
    }
    return module;
  }

  @Override
  @SuppressFBWarnings
  public ClassLoader getClassLoader() {
    if (module == null) {
      throw new IllegalModuleStateException(
          "Module must be in at least the 'RESOLVED' state to perform this operation");
    }

    return new WeakReferenceClassLoader(module.getClassLoader());
  }

  @Override
  public boolean dependsOn(Coordinate m, Transitivity transitivity) {
    return false;
  }

  @Override
  public <S> ServiceLoader<S> resolveServiceLoader(Class<S> type) {
    return module.loadService(type);
  }

  @Override
  public int compareTo(@NotNull io.sunshower.kernel.Module o) {
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
