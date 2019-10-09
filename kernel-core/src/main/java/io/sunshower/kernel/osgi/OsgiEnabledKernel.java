package io.sunshower.kernel.osgi;

import io.sunshower.common.i18n.Localization;
import io.sunshower.common.io.MonitorableByteChannel;
import io.sunshower.common.io.MonitorableChannels;
import io.sunshower.kernel.PluginRegistration;
import io.sunshower.kernel.PluginRegistrationException;
import io.sunshower.kernel.Kernel;
import io.sunshower.kernel.launch.KernelOptions;
import lombok.AllArgsConstructor;
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
public class OsgiEnabledKernel implements Kernel {
    final KernelOptions options;
    final Localization  localization;


    private final List<PluginListener> downloadingPlugins;


    public OsgiEnabledKernel(final KernelOptions options) {
        this.options = options;
        this.localization = options.getLocalization();
        downloadingPlugins = new ArrayList<>();
    }


    @Override
    public CompletableFuture<PluginRegistration> install(URL url) {
        val target = new File(options.getStorage(), getFilename(url));
        try {
            val result = new CompletableFuture<PluginListener>();
            val callable = MonitorableChannels.transfer(url, target);
            callable.addListener(new PluginListener(url, target, result));
            options.getExecutorService().submit(callable);
            return result.thenApplyAsync(this::extractPlugin);
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

    private PluginRegistration extractPlugin(PluginListener installation)  {
        return new OsgiPluginRegistration(this, installation.source, installation.destination);
    }


    private String getFilename(URL url) {
        val p = url.getPath();
        return p.substring(p.lastIndexOf('/') + 1, p.length());
    }

    @AllArgsConstructor
    private final class PluginListener implements MonitorableByteChannel.Listener {
        final URL source;
        final File destination;
        final CompletableFuture<PluginListener> future;

        @Override
        public void onTransfer(ReadableByteChannel channel, double progress) {

        }

        @Override
        public void onComplete(ReadableByteChannel channel) {
            log.info(localization.format("plugin.download.complete", source, destination));
            future.complete(this);
        }

        @Override
        public void onError(ReadableByteChannel channel, Exception ex) {
            log.info(localization.format("plugin.download.error", source, destination));
            future.obtrudeException(ex);
        }

        @Override
        public void onCancel(ReadableByteChannel channel) {
            log.info(localization.format("plugin.download.cancel", source, destination));
            future.cancel(true);
        }
    }
}
