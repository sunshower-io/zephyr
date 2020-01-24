package io.sunshower.kernel.test;

import io.sunshower.test.common.Tests;
import io.zephyr.api.ModuleContext;
import io.zephyr.api.PluginActivator;
import io.zephyr.kernel.*;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.core.AbstractModule;
import io.zephyr.kernel.core.ModuleClasspath;
import io.zephyr.kernel.core.ModuleCoordinate;
import io.zephyr.kernel.core.ModuleSource;
import io.zephyr.kernel.memento.Memento;
import io.zephyr.kernel.module.ModuleLifecycle;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.springframework.context.ApplicationContext;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Collections;
import java.util.ServiceLoader;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

/** this module simulates an actual module, but uses the test classpath instead */
public class SimulatedModule extends AbstractModule implements Module {

  /** immmutable state */
  final Memento memento;

  final Module.Type type;
  final Lifecycle lifecycle;
  final Coordinate coordinate;
  final SimulatedModuleLoader loader;
  private final ApplicationContext applicationContext;

  /** mutable state */
  @Getter @Setter private ModuleThread thread;

  public SimulatedModule(Module.Type type, ApplicationContext context) {
    this.type = type;
    this.applicationContext = context;
    this.memento = mock(Memento.class);
    this.lifecycle = new ModuleLifecycle(this);
    this.lifecycle.setState(Lifecycle.State.Installed);
    this.loader = new SimulatedModuleLoader();
    this.coordinate = ModuleCoordinate.create("test", "test", "1.0.0-SNAPSHOT");
  }

  @Override
  public TaskQueue getTaskQueue() {
    return thread;
  }

  @Override
  public ModuleClasspath getModuleClasspath() {
    return loader.classpath;
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
    return Tests.buildDirectory().toPath();
  }

  @Override
  public Assembly getAssembly() {
    return new Assembly(getModuleDirectory().toFile());
  }

  @Override
  @SneakyThrows
  public Source getSource() {
    return new ModuleSource(getModuleDirectory().toUri());
  }

  @Override
  public Lifecycle getLifecycle() {
    return lifecycle;
  }

  @Override
  public PluginActivator getActivator() {
    return applicationContext.getBean(PluginActivator.class);
  }

  @Override
  public Coordinate getCoordinate() {
    return coordinate;
  }

  @Override

  public FileSystem getFileSystem() {
    return spy(FileSystem.class);
  }

  @Override
  public ClassLoader getClassLoader() {
    return ClassLoader.getSystemClassLoader();
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
  public void setActivator(PluginActivator o) {}
}
