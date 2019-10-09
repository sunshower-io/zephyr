package io.sunshower.kernel;

import io.sunshower.kernel.io.ChannelTransferListener;

import java.util.concurrent.CompletableFuture;

public interface PluginLoadTask extends ChannelTransferListener, KernelExtensionLoadTask {

    enum State {
        Paused,
        Completed,
        Error, Cancelled, Running
    }

    boolean isComplete();

    void pause();

    void resume();

    PluginLoadTask restart();

    void cancel();

    State getState();

    Throwable getError();

    CompletableFuture<PluginDescriptor> getFuture();
}
