package io.sunshower.kernel.test;

import io.sunshower.PluginActivator;
import io.sunshower.kernel.*;
import io.sunshower.kernel.Module;
import io.sunshower.kernel.core.ModuleClasspath;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class MockModule implements Module {

  final Coordinate coordinate;
  final List<Dependency> dependencies;

  public void addDependency(Dependency dependency) {
    dependencies.add(dependency);
  }

  @Override
  public ModuleClasspath getModuleClasspath() {
    return null;
  }

  @Override
  public int getOrder() {
    return 0;
  }

  @Override
  public Set<Library> getLibraries() {
    return null;
  }

  @Override
  public Module.Type getType() {
    return null;
  }

  @Override
  public Path getModuleDirectory() {
    return null;
  }

  @Override
  public Assembly getAssembly() {
    return null;
  }

  @Override
  public Source getSource() {
    return null;
  }

  @Override
  public Lifecycle getLifecycle() {
    return null;
  }

  @Override
  public PluginActivator getActivator() {
    return null;
  }

  @Override
  public Coordinate getCoordinate() {
    return coordinate;
  }

  @Override
  public FileSystem getFileSystem() {
    return null;
  }

  @Override
  public ClassLoader getClassLoader() {
    return null;
  }

  @Override
  public Set<Dependency> getDependencies() {
    return new HashSet<>(dependencies);
  }

  @Override
  public boolean dependsOn(Coordinate m, Transitivity transitivity) {
    return false;
  }

  @Override
  public <S> ServiceLoader<S> resolveServiceLoader(Class<S> type) {
    return null;
  }
}
