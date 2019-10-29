package io.sunshower.kernel;

import io.sunshower.kernel.core.KernelException;
import java.util.Collection;
import java.util.Set;
import lombok.Getter;

@Getter
public class UnsatisfiedDependencyException extends KernelException {

  static final int serialVersionUID = 1234134;

  public UnsatisfiedDependencyException(final Exception cause) {
    super(cause);
  }

  public UnsatisfiedDependencyException(Module module, Collection<Dependency> dependencies) {}

  public UnsatisfiedDependencyException(Module module, Set<Coordinate> unresolved) {}
}
