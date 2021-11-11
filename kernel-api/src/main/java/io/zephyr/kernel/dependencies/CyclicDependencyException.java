package io.zephyr.kernel.dependencies;

import io.sunshower.gyre.Component;
import io.sunshower.gyre.DirectedGraph;
import io.zephyr.kernel.Coordinate;
import io.zephyr.kernel.PluginException;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;

public class CyclicDependencyException extends PluginException {

  @Getter private final Set<Component<DirectedGraph.Edge<Coordinate>, Coordinate>> components;

  public CyclicDependencyException() {
    components = new LinkedHashSet<>();
  }

  public CyclicDependencyException(String message) {
    super(message);
    components = new LinkedHashSet<>();
  }

  public CyclicDependencyException(String message, Throwable cause) {
    super(message, cause);
    components = new LinkedHashSet<>();
  }

  public CyclicDependencyException(Throwable cause) {
    super(cause);
    components = new LinkedHashSet<>();
  }

  public CyclicDependencyException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
    components = new LinkedHashSet<>();
  }

  public void addComponent(Component<DirectedGraph.Edge<Coordinate>, Coordinate> component) {
    components.add(component);
  }
}
