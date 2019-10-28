package io.sunshower.kernel;

import io.sunshower.kernel.core.KernelException;
import io.sunshower.kernel.dependencies.Components;

public class CyclicModuleDependencyException extends KernelException {

  static final int serialVersionUID = 1234123324;

  final Module module;
  final Components components;

  public CyclicModuleDependencyException(Module module, Components components) {
    this.module = module;
    this.components = components;
  }
}
