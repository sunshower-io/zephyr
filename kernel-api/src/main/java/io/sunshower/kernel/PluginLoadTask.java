package io.sunshower.kernel;

import io.sunshower.kernel.io.ChannelTransferListener;
import io.sunshower.kernel.io.ObservableChannelTransferListener;

import java.util.concurrent.CompletableFuture;

public interface PluginLoadTask
        extends ChannelTransferListener,
        KernelExtensionLoadTask<PluginDescriptor, PluginLoadTask> {

}
