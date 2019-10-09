package io.sunshower.kernel.osgi;

import io.sunshower.common.i18n.Localization;
import io.sunshower.common.io.MonitorableChannels;
import io.sunshower.kernel.*;
import io.sunshower.kernel.io.ObservableChannelTransferListener;
import io.sunshower.kernel.launch.KernelOptions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class OsgiPluginManager implements PluginManager {

    private final KernelOptions     options;
    private final OsgiEnabledKernel kernel;
    private final Localization      localization;


    private final List<PluginLoadTask> loadingPlugins;

    public OsgiPluginManager(
            KernelOptions options,
            OsgiEnabledKernel kernel,
            Localization localization
    ) {
        this.options = options;
        this.kernel = kernel;
        this.localization = localization;
        this.loadingPlugins = new ArrayList<>();
    }

    @Override
    public List<PluginDescriptor> getLoaded() {
        return null;
    }

    @Override
    public boolean unload(PluginDescriptor descriptor) throws PluginException {
        return false;
    }

    @Override
    public PluginLoadTask load(URL url) throws PluginConflictException {
        val target = new File(options.getStorage(), getFilename(url));
        try {
            val callable = MonitorableChannels.transfer(url, target);
            val result   = new PluginListener(url, target);
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

    private PluginDescriptor extractPlugin(PluginListener installation) {
        final PluginDescriptor pluginDescriptor = new PluginDescriptor(){};
        return pluginDescriptor;
    }


    private String getFilename(URL url) {
        val p = url.getPath();
        return p.substring(p.lastIndexOf('/') + 1, p.length());
    }

    @AllArgsConstructor
    private final class PluginListener extends ObservableChannelTransferListener implements PluginLoadTask {
        final URL                               source;
        final File                              destination;
        final CompletableFuture<PluginListener> future;

        private State state;

        private volatile boolean paused;

        @Getter
        private Throwable error;

        PluginListener(URL source, File target) {
            this.source = source;
            this.destination = target;
            this.future = new CompletableFuture<>();
        }


        int count = 0;
        @Override
        public void onTransfer(ReadableByteChannel channel, double progress) {
            if(count++ % 100 == 0) {
                System.out.println(progress);

            }
        }

        @Override
        public void onComplete(ReadableByteChannel channel) {
            log.info(localization.format("plugin.download.complete", source, destination));
            future.complete(this);
            this.state = State.Completed;
            super.onComplete(channel);
            loadingPlugins.remove(this);
        }

        @Override
        public void onError(ReadableByteChannel channel, Exception ex) {
            log.info(localization.format("plugin.download.error", source, destination));
            future.obtrudeException(ex);
            this.error = ex;
            this.state = State.Error;
            super.onError(channel, ex);
        }

        @Override
        public void onCancel(ReadableByteChannel channel) {
            log.info(localization.format("plugin.download.cancel", source, destination));
            future.cancel(true);
            this.state = State.Cancelled;
            super.onCancel(channel);
        }

        @Override
        public boolean isComplete() {
            return state == State.Completed;
        }

        @Override
        public synchronized void pause() {
            throw new UnsupportedOperationException("Cannot currently pause plugin download");
        }

        @Override
        public synchronized void resume() {
            throw new UnsupportedOperationException("Cannot currently resume plugin download");
        }

        @Override
        public PluginLoadTask restart() {
            cancel();
            removeListener(this);
            return load(source);
        }

        @Override
        public void cancel() {
            log.info(localization.format("plugin.cancelling", source));
            Thread.currentThread().interrupt();
            if (!destination.delete()) {
                log.warn(localization.format("cancelled.plugin.temp.deletion.failed", source, destination));
            }
            log.info(localization.format("plugin.cancelled", source));
            state = State.Cancelled;
        }

        @Override
        public State getState() {
            return state;
        }

        @Override
        public CompletableFuture<PluginDescriptor> getFuture() {
            return future.thenApplyAsync(OsgiPluginManager.this::extractPlugin);
        }
    }
}
