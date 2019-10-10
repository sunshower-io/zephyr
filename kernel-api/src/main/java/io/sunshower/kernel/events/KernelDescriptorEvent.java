package io.sunshower.kernel.events;

import io.sunshower.kernel.KernelExtension;
import io.sunshower.kernel.KernelExtensionDescriptor;
import io.sunshower.kernel.KernelExtensionManager;
import lombok.Getter;

public class KernelDescriptorEvent implements KernelExtensionEvent {

  @Getter private final boolean success;

  @Getter private final KernelExtension.State state;

  @Getter private final KernelExtensionManager source;

  @Getter private final KernelExtensionDescriptor cause;

  @Getter private final Throwable error;

  public KernelDescriptorEvent(
      boolean success,
      KernelExtension.State state,
      KernelExtensionManager source,
      KernelExtensionDescriptor cause) {
    this(success, state, source, cause, null);
  }

  public KernelDescriptorEvent(
      boolean success,
      KernelExtension.State state,
      KernelExtensionManager source,
      KernelExtensionDescriptor cause,
      Throwable error) {
    this.success = success;
    this.state = state;
    this.source = source;
    this.cause = cause;
    this.error = error;
  }
}
