package io.sunshower.kernel;

import java.net.URL;
import java.util.List;

public interface KernelModuleManager extends KernelExtensionManager<KernelModuleDescriptor> {

    @Override
    List<KernelModuleDescriptor> getLoaded();

    @Override
    boolean unload(KernelModuleDescriptor descriptor);

    @Override
    KernelExtensionLoadTask load(URL url) throws KernelModuleConflictException;
}
