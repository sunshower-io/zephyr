package io.sunshower.kernel.dependencies;

import io.sunshower.gyre.Component;
import io.sunshower.gyre.DirectedGraph;
import io.sunshower.kernel.Module;
import io.sunshower.kernel.PluginException;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

public class CyclicDependencyException extends PluginException {

  @Getter private final Set<Component<DirectedGraph.Edge<String>, Module>> components;

  public CyclicDependencyException() {
    components = new HashSet<>();
  }

  public CyclicDependencyException(String message) {
    super(message);
    components = new HashSet<>();
  }

  public CyclicDependencyException(String message, Throwable cause) {
    super(message, cause);
    components = new HashSet<>();
  }

  public CyclicDependencyException(Throwable cause) {
    super(cause);
    components = new HashSet<>();
  }

  public CyclicDependencyException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
    components = new HashSet<>();
  }

  public void addComponent(Component<DirectedGraph.Edge<String>, Module> component) {
    components.add(component);
  }
}
