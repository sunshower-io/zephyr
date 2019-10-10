package io.sunshower.kernel;

import io.sunshower.kernel.events.KernelModuleEvent;
import java.net.URL;
import java.util.List;

public interface KernelModuleManager
    extends KernelExtensionManager<
        KernelEventListener, KernelModuleEvent, KernelModuleDescriptor, KernelModuleLoadTask> {

  @Override
  List<KernelModuleDescriptor> getLoaded();

  @Override
  boolean unload(KernelModuleDescriptor descriptor);

  @Override
  KernelModuleLoadTask loadExtensionFile(URL url) throws KernelModuleConflictException;
}
