package io.sunshower.module.phases;

import io.sunshower.kernel.process.AbstractPhase;
import io.sunshower.kernel.process.KernelProcessContext;
import io.sunshower.kernel.process.KernelProcessEvent;

public class ModuleScanPhase extends AbstractPhase<KernelProcessEvent, KernelProcessContext> {
  enum EventType implements KernelProcessEvent {}

  public ModuleScanPhase() {
    super(EventType.class);
  }
}
