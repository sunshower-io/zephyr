package io.sunshower.kernel.osgi;

import io.sunshower.kernel.KernelExtensionManager;
import io.sunshower.kernel.PluginDescriptor;
import io.sunshower.kernel.PluginLoadTask;
import io.sunshower.kernel.PluginManager;
import io.sunshower.kernel.io.ChannelTransferListener;

import java.io.File;
import java.net.URL;

public class PluginDescriptorLoadTask extends AbstractKernelExtensionLoadTask<PluginDescriptor, PluginLoadTask> implements ChannelTransferListener, PluginLoadTask {


    @SuppressWarnings("unchecked")
    public PluginDescriptorLoadTask(
            PluginManager manager,
            URL source,
            File target) {
        super(manager, source, target);
    }


    @Override
    protected PluginDescriptor extract(PluginLoadTask task) {
        return null;
    }
}
