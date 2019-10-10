package io.sunshower.kernel.events;

import io.sunshower.kernel.KernelExtension;
import io.sunshower.kernel.KernelExtensionDescriptor;
import io.sunshower.kernel.KernelExtensionManager;

public interface KernelExtensionEvent extends KernelEvent<KernelExtensionManager> {

  /** @return the extension state */
  KernelExtension.State getState();

  /** @return if this event was successful */
  boolean isSuccess();

  /**
   * isSuccess() return true only if getError() returns false
   *
   * @return the error, if any
   */
  Throwable getError();

  /**
   * What caused this?
   *
   * @return the extension that caused this event during process
   */
  KernelExtensionDescriptor getCause();
}
