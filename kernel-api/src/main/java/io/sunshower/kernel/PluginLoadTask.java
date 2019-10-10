package io.sunshower.kernel;

import io.sunshower.kernel.io.ChannelTransferListener;

public interface PluginLoadTask
    extends ChannelTransferListener, KernelExtensionLoadTask<PluginDescriptor, PluginLoadTask> {}
