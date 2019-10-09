package io.sunshower.kernel.osgi;

import io.sunshower.common.io.MonitorableChannels;
import io.sunshower.kernel.*;
import io.sunshower.kernel.common.i18n.Localization;
import io.sunshower.kernel.io.ChannelTransferListener;
import io.sunshower.kernel.launch.KernelOptions;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public abstract class AbstractKernelExtensionManager<
        T extends KernelExtensionDescriptor,
        U extends KernelExtensionLoadTask<T, U>
                & ChannelTransferListener> implements KernelExtensionManager<T, U> {




    private final KernelOptions     options;
    private final OsgiEnabledKernel kernel;
    private final Localization      localization;
    private final List<U>           loadingTasks;

    public AbstractKernelExtensionManager(
            KernelOptions options,
            OsgiEnabledKernel kernel,
            Localization localization
    ) {
        this.options = options;
        this.kernel = kernel;
        this.localization = localization;
        this.loadingTasks = new ArrayList<>();
    }


    @Override
    public List<T> getLoaded() {
        return null;
    }

    @Override
    public boolean unload(T descriptor) throws PluginException {
        return false;
    }

    @Override
    public U load(URL url) throws PluginConflictException {
        val target = new File(options.getStorage(), getFilename(url));
        try {
            val callable = MonitorableChannels.transfer(url, target);
            val result   = create(url, target);
            callable.addListener(result);
            options.getExecutorService().submit(callable);
            return result;
        } catch (IOException ex) {
            throw new PluginRegistrationException(
                    ex,
                    localization.format(
                            "plugin.registration.error",
                            url,
                            ex
                    ), url
            );
        }
    }

    protected abstract U create(URL url, File destination);


    private static String getFilename(URL url) {
        val p = url.getPath();
        return p.substring(p.lastIndexOf('/') + 1);
    }

}
