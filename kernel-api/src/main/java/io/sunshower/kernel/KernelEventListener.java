package io.sunshower.kernel;

import io.sunshower.kernel.events.KernelModuleEvent;

public interface KernelEventListener {

  default void onKernelModuleEvent(KernelModuleEvent event) {};
}
