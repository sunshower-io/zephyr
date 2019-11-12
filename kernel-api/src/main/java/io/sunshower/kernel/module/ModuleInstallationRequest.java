package io.sunshower.kernel.module;

import io.sunshower.kernel.Coordinate;
import java.net.URL;
import lombok.Setter;

@Setter
public class ModuleInstallationRequest implements ModuleRequest {

  private URL location;
  private Coordinate coordinate;
  private ModuleLifecycle.Actions lifecycleActions;

  public URL getLocation() {
    return location;
  }

  public ModuleLifecycle.Actions getLifecycleActions() {
    return lifecycleActions;
  }

  public Coordinate getCoordinate() {
    return coordinate;
  }

  public void setCoordinate(Coordinate coordinate) {
    this.coordinate = coordinate;
  }
}
