package io.zephyr.spring.embedded;

import io.zephyr.api.ModuleActivator;
import io.zephyr.kernel.*;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.concurrency.ModuleThread;
import io.zephyr.kernel.core.*;
import io.zephyr.kernel.memento.Memento;
import io.zephyr.kernel.module.ModuleLifecycle;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Collections;
import java.util.ServiceLoader;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.springframework.context.ApplicationContext;

/** this module simulates an actual module, but uses the test classpath instead */
public class EmbeddedModule extends AbstractModule implements Module {

  /** immmutable state */
  final Memento memento;

  final Module.Type type;
  final Lifecycle lifecycle;
  final FileSystem fileSystem;
  private ModuleClasspath classpath;
  final ModuleDescriptor descriptor;
  private final ApplicationContext applicationContext;

  /** mutable state */
  @Getter @Setter private ModuleThread thread;

  public EmbeddedModule(
      Module.Type type,
      ApplicationContext context,
      Memento memento,
      ModuleClasspath classpath,
      FileSystem fileSystem,
      ModuleDescriptor descriptor) {
    this.type = type;
    this.memento = memento;
    this.classpath = classpath;
    this.fileSystem = fileSystem;
    this.applicationContext = context;
    this.lifecycle = new ModuleLifecycle(this);
    this.lifecycle.setState(Lifecycle.State.Installed);
    this.descriptor = descriptor;
  }

  @Override
  public TaskQueue getTaskQueue() {
    return thread;
  }

  @Override
  public ModuleClasspath getModuleClasspath() {
    return classpath;
  }

  @Override
  public int getOrder() {
    return 0;
  }

  @Override
  public Set<Library> getLibraries() {
    return Collections.emptySet();
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public Path getModuleDirectory() {
    return null;
    //    return Tests.buildDirectory().toPath();
  }

  @Override
  public Assembly getAssembly() {
    if (getModuleDirectory() == null) {
      return null;
    }
    return new Assembly(getModuleDirectory().toFile());
  }

  @Override
  @SneakyThrows
  public Source getSource() {
    if (getModuleDirectory() == null) {
      return null;
    }
    return new ModuleSource(getModuleDirectory().toUri());
  }

  @Override
  public Lifecycle getLifecycle() {
    return lifecycle;
  }

  @Override
  public ModuleActivator getActivator() {
    return applicationContext.getBean(ModuleActivator.class);
  }

  @Override
  public Coordinate getCoordinate() {
    return descriptor.getCoordinate();
  }

  @Override
  public FileSystem getFileSystem() {
    return fileSystem;
  }

  @Override
  public ClassLoader getClassLoader() {
    return classpath.getClassLoader();
  }

  @Override
  public Set<Dependency> getDependencies() {
    return Collections.emptySet();
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
  public Memento save() {
    return memento;
  }

  @Override
  public void restore(Memento memento) {}

  @Override
  public void setModuleClasspath(ModuleClasspath classpath) {
    // this shouldn't be necessary--the embedded classpath should be correct
    //    this.classpath = classpath;
  }

  @Override
  public void setActivator(ModuleActivator o) {}

  @Override
  public void setTaskQueue(TaskQueue thread) {}
}
