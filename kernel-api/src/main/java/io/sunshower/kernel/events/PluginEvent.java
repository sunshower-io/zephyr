package io.sunshower.kernel.events;

import io.sunshower.kernel.KernelExtension;
import io.sunshower.kernel.KernelExtensionDescriptor;
import io.sunshower.kernel.KernelExtensionManager;

public class PluginEvent extends KernelDescriptorEvent {

  public PluginEvent(
      boolean success,
      KernelExtension.State state,
      KernelExtensionManager source,
      KernelExtensionDescriptor cause) {
    super(success, state, source, cause);
  }
}
