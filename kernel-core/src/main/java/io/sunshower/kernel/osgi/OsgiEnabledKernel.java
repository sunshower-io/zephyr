package io.sunshower.kernel.osgi;

import io.sunshower.kernel.*;
import io.sunshower.kernel.common.i18n.Localization;
import io.sunshower.kernel.launch.KernelOptions;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OsgiEnabledKernel implements Kernel {
    final KernelOptions options;
    final Localization  localization;


    private PluginManager pluginManager;
    private KernelModuleManager moduleManager;


    public OsgiEnabledKernel(final KernelOptions options) {
        this.options = options;
        this.localization = options.getLocalization();
    }



    @Override
    public synchronized PluginManager getPluginManager() {
        if(pluginManager == null) {
            pluginManager = createPluginManager();
        }
        return pluginManager;
    }


    @Override
    public synchronized KernelModuleManager getModuleManager() {
        if(moduleManager == null) {
            moduleManager = createModuleManager();
        }
        return moduleManager;
    }

    private KernelModuleManager createModuleManager() {
        return new DynamicReloadableKernelModuleManager(options, this, localization);
    }

    @Override
    public LifecycleManager getLifecycleManager() {
        return null;
    }

    protected PluginManager createPluginManager() {
        return new OsgiPluginManager(options, this, localization);
    }

}
