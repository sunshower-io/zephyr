package io.sunshower.kernel.modules.descriptors;

import io.sunshower.kernel.Coordinate;
import io.sunshower.kernel.graph.Dependency;

public class YamlDependency implements Dependency {

  public boolean required;

  public String group;

  public String artifact;

  public String version;

  private Coordinate coordinate;

  @Override
  public boolean isRequired() {
    return required;
  }

  @Override
  public Coordinate getCoordinate() {
    if (coordinate == null) {
      coordinate = new Coordinate(group, artifact, version);
    }
    return coordinate;
  }
}
