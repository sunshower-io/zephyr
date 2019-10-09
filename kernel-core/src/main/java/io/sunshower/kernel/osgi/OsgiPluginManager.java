package io.sunshower.kernel.osgi;

import io.sunshower.kernel.*;
import io.sunshower.kernel.common.i18n.Localization;
import io.sunshower.kernel.launch.KernelOptions;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URL;
import java.util.List;

@Slf4j
public class OsgiPluginManager extends
        AbstractKernelExtensionManager<
                PluginDescriptor,
                PluginLoadTask
                > implements PluginManager {


    public OsgiPluginManager(
            KernelOptions options,
            OsgiEnabledKernel kernel,
            Localization localization
    ) {
        super(options, kernel, localization);
    }

    public PluginLoadTask load(URL url) {
        return super.load(url);
    }

    @Override
    protected PluginDescriptorLoadTask create(URL url, File destination) {
        return null;
    }

    @Override
    public List<PluginLoadTask> getInflight() {
        return null;
    }

    @Override
    public Localization getLocalization() {
        return null;
    }
}
