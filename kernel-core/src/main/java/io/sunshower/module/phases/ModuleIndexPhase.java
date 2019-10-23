package io.sunshower.module.phases;

import io.sunshower.kernel.process.AbstractPhase;
import io.sunshower.kernel.process.KernelProcessContext;
import io.sunshower.kernel.process.KernelProcessEvent;

public class ModuleIndexPhase extends AbstractPhase<KernelProcessEvent, KernelProcessContext> {
  enum EventType implements KernelProcessEvent {}

  public ModuleIndexPhase() {
    super(EventType.class);
  }
}
