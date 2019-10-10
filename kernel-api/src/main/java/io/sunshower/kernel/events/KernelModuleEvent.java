package io.sunshower.kernel.events;

import io.sunshower.kernel.KernelExtension;
import io.sunshower.kernel.KernelExtensionDescriptor;
import io.sunshower.kernel.KernelExtensionManager;

public class KernelModuleEvent extends KernelDescriptorEvent {
  public KernelModuleEvent(
      boolean success,
      KernelExtension.State state,
      KernelExtensionManager source,
      KernelExtensionDescriptor cause) {
    super(success, state, source, cause);
  }

  public KernelModuleEvent(
      boolean success,
      KernelExtension.State state,
      KernelExtensionManager source,
      KernelExtensionDescriptor cause,
      Throwable error) {
    super(success, state, source, cause, error);
  }
}
