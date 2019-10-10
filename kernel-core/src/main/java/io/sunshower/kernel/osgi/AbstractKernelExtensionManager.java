package io.sunshower.kernel.osgi;

import io.sunshower.common.io.MonitorableChannels;
import io.sunshower.common.io.MonitorableFileTransfer;
import io.sunshower.kernel.*;
import io.sunshower.kernel.common.i18n.Localization;
import io.sunshower.kernel.io.ChannelTransferListener;
import io.sunshower.kernel.launch.KernelOptions;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public abstract class AbstractKernelExtensionManager<
        T extends KernelExtensionDescriptor,
        U extends KernelExtensionLoadTask<T, U> & ChannelTransferListener>
    implements KernelExtensionManager<T, U> {

  private final KernelOptions options;
  private final OsgiEnabledKernel kernel;
  private final Localization localization;
  private final List<U> loadingTasks;

  private final Map<Coordinate, T> loaded;

  public AbstractKernelExtensionManager(
      KernelOptions options, OsgiEnabledKernel kernel, Localization localization) {
    this.options = options;
    this.kernel = kernel;
    this.localization = localization;
    this.loaded = new HashMap<>();
    this.loadingTasks = new ArrayList<>();
  }

  @Override
  public List<T> getLoaded() {
    return List.copyOf(loaded.values());
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
      val result = create(url, target, callable, options.getExecutorService());
      callable.addListener(result);
      loadingTasks.add(result);
      return result;
    } catch (IOException ex) {
      throw new PluginRegistrationException(
          ex, localization.format("plugin.registration.error", url, ex), url);
    }
  }

  @Override
  public List<U> getInflight() {
    return loadingTasks;
  }

  @Override
  public Localization getLocalization() {
    return localization;
  }

  @Override
  public boolean register(T descriptor) throws KernelExtensionConflictException {
    Objects.requireNonNull(descriptor);
    val coordinate = descriptor.getCoordinate();
    if (loaded.containsKey(coordinate)) {
      throw new KernelExtensionConflictException(
          localization.format("kernel.extension.exists", coordinate), descriptor);
    }
    return loaded.put(coordinate, descriptor) == null;
  }

  protected abstract U create(
      URL url, File destination, MonitorableFileTransfer callable, ExecutorService service);

  private static String getFilename(URL url) {
    val p = url.getPath();
    return p.substring(p.lastIndexOf('/') + 1);
  }
}
