package io.zephyr.kernel.core;

import io.zephyr.PluginActivator;
import io.zephyr.kernel.*;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.memento.Memento;
import io.zephyr.kernel.memento.Originator;
import io.zephyr.kernel.misc.SuppressFBWarnings;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.ServiceLoader;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;

public class DefaultModule implements Module, Comparable<Module>, Originator<Module> {

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
    return getModuleClasspath().resolveServiceLoader(type);
  }

  @Override
  public int compareTo(Module o) {
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

    if (o instanceof Module) {
      val other = (Module) o;
      return coordinate.equals(other.getCoordinate());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return coordinate.hashCode();
  }

  @Override
  public Memento<Module> save() {
    Memento<Module> result = loadMemento();

    if (result == null) {
      return null;
    }

    return save(result);
  }

  @Override
  public void restore(Memento<Module> memento) {}

  private Memento<Module> save(Memento<Module> result) {

    result.write("order", order);
    result.write("type", type);
    result.write("source", source.getLocation());

    writeCoordinate(result, coordinate);
    writeAssembly(result);
    writeLibraries(result);
    writeDependencies(result);
    return result;
  }

  private void writeDependencies(Memento<Module> result) {
    val dependenciesMemento = result.child("dependencies", Dependency.class);
    for (val dependency : dependencies) {
      val dependencyMemento = result.child("dependency", Coordinate.class);
      dependenciesMemento.write("type", dependency.getType());
      writeCoordinate(dependencyMemento, dependency.getCoordinate());
    }
  }

  private void writeCoordinate(Memento<?> result, Coordinate coordinate) {
    val coordinateMemento = result.child("coordinate", Coordinate.class);
    coordinateMemento.write("group", coordinate.getGroup());
    coordinateMemento.write("name", coordinate.getName());
    coordinateMemento.write("version", coordinate.getVersion());
  }

  private void writeAssembly(Memento<Module> result) {
    val assemblyMemento = result.child("assembly", Assembly.class);
    assemblyMemento.write("file", assembly.getFile().getAbsolutePath());

    val assemblySubpathsMemento = assemblyMemento.child("paths", Set.class);
    val subpaths = assembly.getSubpaths();
    for (val path : subpaths) {
      val pathMemento = assemblySubpathsMemento.child("path", Path.class);
      pathMemento.setValue(path);
    }
  }

  private void writeLibraries(Memento<Module> result) {
    val librariesMemento = result.child("libraries", Library.class);
    for (val library : libraries) {
      val libraryMemento = librariesMemento.child("library", String.class);
      libraryMemento.setValue(library.getFile().getAbsolutePath());
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private Memento<Module> loadMemento() {
    val loader = resolveServiceLoader(Memento.class).iterator();
    Memento<Module> result = null;
    while (loader.hasNext()) {
      val next = loader.next();
      result = next;
      break;
    }
    return result;
  }
}
