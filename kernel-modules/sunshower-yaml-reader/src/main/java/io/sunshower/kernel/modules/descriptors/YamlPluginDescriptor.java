package io.sunshower.kernel.modules.descriptors;

import io.sunshower.kernel.PluginDescriptor;
import java.net.URL;
import java.nio.file.Path;

public class YamlPluginDescriptor implements PluginDescriptor {
  @Override
  public URL getSource() {
    return null;
  }

  @Override
  public Path getLoadedFile() {
    return null;
  }

  @Override
  public Path getLoadDirectory() {
    return null;
  }

  //  public String name;
  //
  //  public String group;
  //
  //  public String version;
  //
  //  public String description;
  //
  //  private Coordinate coordinate;
  //
  //  public List<DependencyHolder> dependencies;
  //
  //  @Override
  //  public Coordinate getCoordinate() {
  //    if (coordinate == null) {
  //      coordinate = new Coordinate(group, name, version);
  //    }
  //    return coordinate;
  //  }
  //
  //  @Override
  //  public List<Dependency> getDependencies() {
  //    if (dependencies == null) {
  //      return Collections.emptyList();
  //    }
  //    return dependencies.stream().map(t -> t.dependency).collect(Collectors.toList());
  //  }
}
