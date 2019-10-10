package io.sunshower.kernel.modules.descriptors;

import io.sunshower.kernel.Coordinate;
import io.sunshower.kernel.PluginDescriptor;
import io.sunshower.kernel.graph.Dependency;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class YamlPluginDescriptor implements PluginDescriptor {

  public String name;

  public String group;

  public String version;

  public String description;

  private Coordinate coordinate;

  public List<DependencyHolder> dependencies;

  @Override
  public Coordinate getCoordinate() {
    if (coordinate == null) {
      coordinate = new Coordinate(group, name, version);
    }
    return coordinate;
  }

  @Override
  public List<Dependency> getDependencies() {
    if (dependencies == null) {
      return Collections.emptyList();
    }
    return dependencies.stream().map(t -> t.dependency).collect(Collectors.toList());
  }
}
