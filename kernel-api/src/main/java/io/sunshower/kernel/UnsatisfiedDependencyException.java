package io.sunshower.kernel;

import io.sunshower.kernel.core.KernelException;
import lombok.Getter;

@Getter
public class UnsatisfiedDependencyException extends KernelException {

  static final int serialVersionUID = 1234134;

  private final Module module;
  private final Dependency dependency;

  public UnsatisfiedDependencyException(Module module, Dependency dependency) {
    this.module = module;
    this.dependency = dependency;
  }
}
