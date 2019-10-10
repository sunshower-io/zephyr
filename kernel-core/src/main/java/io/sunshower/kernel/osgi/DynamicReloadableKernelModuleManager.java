package io.sunshower.kernel.osgi;

import io.sunshower.common.io.MonitorableFileTransfer;
import io.sunshower.kernel.*;
import io.sunshower.kernel.common.i18n.Localization;
import io.sunshower.kernel.launch.KernelOptions;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class DynamicReloadableKernelModuleManager extends AbstractKernelExtensionManager<
        KernelModuleDescriptor,
        KernelModuleLoadTask
        > implements KernelModuleManager {


    public DynamicReloadableKernelModuleManager(KernelOptions options, OsgiEnabledKernel kernel, Localization localization) {
        super(options, kernel, localization);
    }

    @Override
    protected KernelModuleLoadTask create(URL url, File destination, MonitorableFileTransfer callable, ExecutorService service) {
        return null;
    }

}
