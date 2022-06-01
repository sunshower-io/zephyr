package io.zephyr.kernel.dependencies;

import io.zephyr.kernel.PluginException;
import java.util.Set;
import lombok.Getter;
import lombok.val;

public class UnresolvedDependencyException extends PluginException {

  @Getter private final Set<DependencyGraph.UnsatisfiedDependencySet> unresolvedDependencies;

  public UnresolvedDependencyException(
      Set<DependencyGraph.UnsatisfiedDependencySet> unresolvedDependencies) {
    super(createMessage(null, unresolvedDependencies));
    this.unresolvedDependencies = unresolvedDependencies;
  }

  public UnresolvedDependencyException(
      String message, Set<DependencyGraph.UnsatisfiedDependencySet> unresolvedDependencies) {
    super(createMessage(message, unresolvedDependencies));
    this.unresolvedDependencies = unresolvedDependencies;
  }

  public UnresolvedDependencyException(
      String message,
      Throwable cause,
      Set<DependencyGraph.UnsatisfiedDependencySet> unresolvedDependencies) {
    super(createMessage(message, unresolvedDependencies), cause);
    this.unresolvedDependencies = unresolvedDependencies;
  }

  public UnresolvedDependencyException(
      Throwable cause, Set<DependencyGraph.UnsatisfiedDependencySet> deps) {
    super(createMessage(cause.getMessage(), deps), cause);
    unresolvedDependencies = deps;
  }

  static String createMessage(
      String base, Set<DependencyGraph.UnsatisfiedDependencySet> dependencies) {
    val builder = new StringBuilder(base);
    builder.append(" \n\tunresolved dependencies:\n\t");
    var count = 0;
    for (val dep : dependencies) {
      for (int i = 0; i < count; i++) {
        builder.append(" ");
      }
      builder.append(dep).append("\n");
      count++;
    }
    return builder.toString();
  }

  public void add(DependencyGraph.UnsatisfiedDependencySet unresolvedDependency) {
    unresolvedDependencies.add(unresolvedDependency);
  }
}
