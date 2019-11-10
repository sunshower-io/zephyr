package io.sunshower.kernel.dependencies;

import io.sunshower.kernel.PluginException;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

public class UnresolvedDependencyException extends PluginException {

  @Getter private final Set<DependencyGraph.UnsatisfiedDependencySet> unresolvedDependencies;

  public UnresolvedDependencyException() {
    unresolvedDependencies = new HashSet<>();
  }

  public UnresolvedDependencyException(String message) {
    super(message);
    unresolvedDependencies = new HashSet<>();
  }

  public UnresolvedDependencyException(String message, Throwable cause) {
    super(message, cause);
    unresolvedDependencies = new HashSet<>();
  }

  public UnresolvedDependencyException(Throwable cause) {
    super(cause);
    unresolvedDependencies = new HashSet<>();
  }

  public UnresolvedDependencyException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
    unresolvedDependencies = new HashSet<>();
  }

  public void add(DependencyGraph.UnsatisfiedDependencySet unresolvedDependency) {
    unresolvedDependencies.add(unresolvedDependency);
  }
}
