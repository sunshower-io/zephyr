package io.sunshower.kernel;

import io.sunshower.kernel.io.ChannelTransferListener;

public interface KernelModuleLoadTask
        extends ChannelTransferListener,
        KernelExtensionLoadTask<KernelModuleDescriptor, KernelModuleLoadTask> {
}
