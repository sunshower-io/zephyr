package io.sunshower.kernel.core;

import io.sunshower.kernel.Module;
import io.sunshower.kernel.dependencies.ModuleCycleDetector;

public class CyclicModuleDependencyException extends KernelException {

  static final int serialVersionUID = 1234123324;

  final Module module;
  final ModuleCycleDetector.Components components;

  public CyclicModuleDependencyException(Module module, ModuleCycleDetector.Components components) {
    this.module = module;
    this.components = components;
  }
}
