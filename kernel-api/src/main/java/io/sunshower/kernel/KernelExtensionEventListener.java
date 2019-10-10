package io.sunshower.kernel;

import io.sunshower.kernel.events.KernelExtensionEvent;

public interface KernelExtensionEventListener extends KernelEventListener {

  default void onKernelExtensionFileLoaded(KernelExtensionEvent event) {};
}
