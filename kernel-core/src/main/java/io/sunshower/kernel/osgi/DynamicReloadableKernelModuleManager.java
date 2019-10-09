package io.sunshower.kernel.osgi;

import io.sunshower.kernel.*;

import java.net.URL;
import java.util.List;

public class DynamicReloadableKernelModuleManager implements KernelModuleManager {

    @Override
    public List<KernelModuleDescriptor> getLoaded() {
        return null;
    }

    @Override
    public boolean unload(KernelModuleDescriptor descriptor) {
        return false;
    }


    @Override
    public KernelExtensionLoadTask load(URL url) throws KernelModuleConflictException {
        return null;
    }
}
