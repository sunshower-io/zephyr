package io.sunshower.kernel.osgi;

import io.sunshower.kernel.*;
import io.sunshower.kernel.common.i18n.Localization;

import java.net.URL;
import java.util.List;

public class DynamicReloadableKernelModuleManager implements KernelModuleManager {

    @Override
    public List<KernelModuleLoadTask> getInflight() {
        return null;
    }

    @Override
    public Localization getLocalization() {
        return null;
    }

    @Override
    public List<KernelModuleDescriptor> getLoaded() {
        return null;
    }

    @Override
    public boolean unload(KernelModuleDescriptor descriptor) {
        return false;
    }

    @Override
    public KernelModuleLoadTask load(URL url) throws KernelModuleConflictException {
        return null;
    }
}
