package io.sunshower.module.phases;

import io.sunshower.kernel.process.AbstractPhase;
import io.sunshower.kernel.process.KernelProcessContext;
import io.sunshower.kernel.process.KernelProcessEvent;

public class ModuleUnpackPhase extends AbstractPhase<KernelProcessEvent, KernelProcessContext> {
  enum EventType implements KernelProcessEvent {}

  public ModuleUnpackPhase() {
    super(EventType.class);
  }
}
