package io.sunshower.kernel;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.ServiceLoader;
import java.util.Set;
import lombok.Setter;
import lombok.val;
import org.jboss.modules.Module;
import org.jetbrains.annotations.NotNull;

public class DefaultModule
    implements io.sunshower.kernel.Module, Comparable<io.sunshower.kernel.Module> {

  /**
   * mutable state . These can't be final because either the module isn't resolved or there is a
   * mutual dependency
   */
  @Setter private Module module;

  @Setter private Lifecycle lifecycle;

  private final Type type;

  private final Source source;
  private final Path moduleDirectory;
  private final Coordinate coordinate;
  private final FileSystem fileSystem;

  // must be unmodifiable upon construction
  private final Set<Dependency> dependencies;

  public DefaultModule(
      Type type,
      Source source,
      Path moduleDirectory,
      Coordinate coordinate,
      FileSystem fileSystem,
      Set<Dependency> dependencies) {
    this.type = type;
    this.source = source;
    this.moduleDirectory = moduleDirectory;
    this.coordinate = coordinate;
    this.fileSystem = fileSystem;
    this.dependencies = dependencies;
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public Path getModuleDirectory() {
    return moduleDirectory;
  }

  @Override
  public Source getSource() {
    return source;
  }

  @Override
  public Lifecycle getLifecycle() {
    return lifecycle;
  }

  @Override
  public Coordinate getCoordinate() {
    return coordinate;
  }

  @Override
  public FileSystem getFileSystem() {
    return fileSystem;
  }

  @Override
  public ClassLoader getClassLoader() {
    return module.getClassLoader();
  }

  @Override
  public Set<Dependency> getDependencies() {
    return dependencies;
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
