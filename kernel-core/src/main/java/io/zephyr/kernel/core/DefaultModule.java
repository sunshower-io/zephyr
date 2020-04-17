package io.zephyr.kernel.core;

import static io.zephyr.kernel.memento.Mementos.writeCoordinate;

import io.zephyr.api.ModuleActivator;
import io.zephyr.api.ModuleContext;
import io.zephyr.kernel.*;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.memento.Memento;
import io.zephyr.kernel.memento.Originator;
import io.zephyr.kernel.misc.SuppressFBWarnings;
import java.io.File;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;
import lombok.SneakyThrows;
import lombok.val;

@SuppressWarnings({
  "PMD.AvoidUsingVolatile",
  "PMD.AvoidDuplicateLiterals",
  "PMD.UnusedPrivateMethod",
  "PMD.DataflowAnomalyAnalysis",
  "PMD.AvoidInstantiatingObjectsInLoops"
})
public final class DefaultModule extends AbstractModule
    implements Module, Comparable<Module>, Originator {
  private int order;
  private Type type;

  private Kernel kernel;
  private Source source;
  private Assembly assembly;
  private Path moduleDirectory;
  private Coordinate coordinate;
  private FileSystem fileSystem;
  private Lifecycle lifecycle;

  private ModuleActivator activator;
  private ModuleClasspath moduleClasspath;

  private Set<Library> libraries;
  private Set<Dependency> dependencies;
  private volatile TaskQueue taskQueue;

  public DefaultModule(
      int order,
      Type type,
      Source source,
      Kernel kernel,
      Assembly assembly,
      Path moduleDirectory,
      Coordinate coordinate,
      FileSystem fileSystem,
      Set<Library> libraries,
      Set<Dependency> dependencies) {
    this.type = type;
    this.order = order;
    this.source = source;
    this.kernel = kernel;
    this.assembly = assembly;
    this.libraries = libraries;
    this.coordinate = coordinate;
    this.fileSystem = fileSystem;
    this.dependencies = dependencies;
    this.moduleDirectory = moduleDirectory;
  }

  public void setKernel(Kernel kernel) {
    this.kernel = kernel;
  }

  public DefaultModule() {}

  @Override
  public ModuleLoader getModuleLoader() {
    return moduleLoader;
  }

  @Override
  public void setModuleLoader(ModuleLoader moduleLoader) {
    this.moduleLoader = moduleLoader;
  }

  @Override
  public ModuleActivator getActivator() {
    return activator;
  }

  @Override
  public void setActivator(ModuleActivator activator) {
    this.activator = activator;
  }

  @Override
  public Lifecycle getLifecycle() {
    return lifecycle;
  }

  public void setLifecycle(Lifecycle lifecycle) {
    this.lifecycle = lifecycle;
  }

  @Override
  public void setModuleClasspath(ModuleClasspath moduleClasspath) {
    this.moduleClasspath = moduleClasspath;
  }

  @Override
  public int getOrder() {
    return order;
  }

  public void setOrder(int order) {
    this.order = order;
  }

  @Override
  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  @Override
  public Source getSource() {
    return source;
  }

  public void setSource(Source source) {
    this.source = source;
  }

  @Override
  public Assembly getAssembly() {
    return assembly;
  }

  public void setAssembly(Assembly assembly) {
    this.assembly = assembly;
  }

  @Override
  public Path getModuleDirectory() {
    return moduleDirectory;
  }

  public void setModuleDirectory(Path moduleDirectory) {
    this.moduleDirectory = moduleDirectory;
  }

  @Override
  public Coordinate getCoordinate() {
    return coordinate;
  }

  public void setCoordinate(Coordinate coordinate) {
    this.coordinate = coordinate;
  }

  @Override
  public FileSystem getFileSystem() {
    return fileSystem;
  }

  public void setFileSystem(FileSystem fileSystem) {
    this.fileSystem = fileSystem;
  }

  @Override
  public Set<Library> getLibraries() {
    return libraries;
  }

  public void setLibraries(Set<Library> libraries) {
    this.libraries = libraries;
  }

  @Override
  public Set<Dependency> getDependencies() {
    return dependencies;
  }

  public void setDependencies(Set<Dependency> dependencies) {
    this.dependencies = dependencies;
  }

  @Override
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
  public ModuleContext getContext() {
    return context;
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
  public Memento save() {
    val memento = Memento.load(kernel.getClassLoader());
    return save(memento);
  }

  @Override
  public void restore(Memento memento) {
    this.order = Integer.parseInt(memento.read("order", String.class));
    this.type = Type.parse(memento.read("type", String.class));
    this.source = new ModuleSource(URI.create(memento.read("source", String.class)));

    readCoordinate(memento);
    readAssembly(memento);
    readLibraries(memento);
    readDependencies(memento);
  }

  private void readCoordinate(Memento memento) {
    coordinate = memento.read("coordinate", Coordinate.class);
  }

  private Memento save(Memento result) {

    result.write("order", order);
    result.write("type", type);
    result.write("source", source.getLocation());
    writeCoordinate(result, coordinate);
    writeAssembly(result);
    writeLibraries(result);
    writeDependencies(result);
    return result;
  }

  private void readAssembly(Memento memento) {
    val assemblyMemento = memento.childNamed("assembly");
    val file = assemblyMemento.read("file", String.class);
    assembly = new Assembly(new File(file));
    val pathMemento = assemblyMemento.childNamed("paths");
    val paths = pathMemento.getChildren("path");

    for (val path : paths) {
      assembly.addSubpath(String.valueOf(path.getValue()));
    }
  }

  private void writeAssembly(Memento result) {
    val assemblyMemento = result.child("assembly");
    assemblyMemento.write("file", assembly.getFile().getAbsolutePath());

    val assemblySubpathsMemento = assemblyMemento.child("paths");
    val subpaths = assembly.getSubpaths();
    for (val path : subpaths) {
      val pathMemento = assemblySubpathsMemento.child("path");
      pathMemento.setValue(path);
    }
  }

  private void writeDependencies(Memento result) {
    val dependenciesMemento = result.child("dependencies");
    for (val dependency : dependencies) {
      val dependencyMemento = dependenciesMemento.child("dependency");
      dependencyMemento.write("type", dependency.getType());
      writeCoordinate(dependencyMemento, dependency.getCoordinate());
    }
  }

  private void readDependencies(Memento memento) {
    dependencies = new HashSet<>();
    val dependenciesMemento = memento.childNamed("dependencies");
    val depList = dependenciesMemento.getChildren("dependency");
    for (val dep : depList) {
      val depType = Dependency.Type.parse(dep.read("type", String.class));
      val coordinate = dep.read("coordinate", Coordinate.class);
      dependencies.add(new Dependency(depType, coordinate));
    }
  }

  private void readLibraries(Memento memento) {
    libraries = new HashSet<>();
    val librariesMemento = memento.childNamed("libraries");
    val children = librariesMemento.getChildren("library");
    for (val library : children) {
      libraries.add(new Library(new File(String.valueOf(library.getValue()))));
    }
  }

  private void writeLibraries(Memento result) {
    val librariesMemento = result.child("libraries");
    for (val library : libraries) {
      val libraryMemento = librariesMemento.child("library");
      libraryMemento.setValue(library.getFile().getAbsolutePath());
    }
  }

  @Override
  public String toString() {
    return "Module{" + getCoordinate() + "}";
  }

  @Override
  public void setTaskQueue(TaskQueue taskQueue) {
    this.taskQueue = taskQueue;
  }

  @Override
  public TaskQueue getTaskQueue() {
    return taskQueue;
  }
}
