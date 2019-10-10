package io.sunshower.kernel.osgi;

import io.sunshower.common.io.MonitorableChannels;
import io.sunshower.common.io.MonitorableFileTransfer;
import io.sunshower.kernel.*;
import io.sunshower.kernel.common.i18n.Localization;
import io.sunshower.kernel.events.KernelDescriptorEvent;
import io.sunshower.kernel.events.KernelExtensionEvent;
import io.sunshower.kernel.io.ChannelTransferListener;
import io.sunshower.kernel.launch.KernelOptions;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public abstract class AbstractKernelExtensionManager<
        L extends KernelEventListener,
        E extends KernelExtensionEvent,
        T extends KernelExtensionDescriptor,
        U extends KernelExtensionLoadTask<T, U> & ChannelTransferListener>
    extends AbstractEventDispatcher<L, E> implements KernelExtensionManager<L, E, T, U> {

  private final KernelOptions options;
  private final OsgiEnabledKernel kernel;
  private final Localization localization;
  private final List<U> loadingTasks;

  private final SortedSet<T> loadedExtensions;

  public AbstractKernelExtensionManager(
      KernelOptions options, OsgiEnabledKernel kernel, Localization localization) {
    this.options = options;
    this.kernel = kernel;
    this.localization = localization;
    this.loadingTasks = new ArrayList<>();
    this.loadedExtensions = new ConcurrentSkipListSet<>(new SourceSortingExtensionComparator<>());
  }

  @Override
  public List<T> getLoaded() {
    return new ArrayList<>(loadedExtensions);
  }

  @Override
  public U loadExtensionFile(URL url) {
    val target = new File(getLoadLocation(options), getFilename(url));
    try {
      val callable = MonitorableChannels.transfer(url, target);
      val result = create(url, target, callable, options.getExecutorService());
      callable.addListener(result);
      loadingTasks.add(result);
      return result;
    } catch (IOException ex) {
      val cause =
          new AbstractKernelExtensionDescriptor(
              url, target.toPath(), new File(getLoadLocation(options)).toPath());
      throw new KernelExtensionException(
          localization.format("plugin.registration.error", url, ex), ex, cause);
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
  public boolean unload(T descriptor) {
    return false;
  }

  @Override
  public boolean registerExtension(T descriptor) {
    return false;
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean registerDescriptor(T descriptor) {
    val result = loadedExtensions.add(descriptor);
    dispatchEvent(
        (E) new KernelDescriptorEvent(result, KernelExtension.State.Loaded, this, descriptor));
    return result;
  }

  protected abstract String getLoadLocation(KernelOptions options);

  protected abstract U create(
      URL url, File destination, MonitorableFileTransfer callable, ExecutorService service);

  private static String getFilename(URL url) {
    val p = url.getPath();
    return p.substring(p.lastIndexOf('/') + 1);
  }
}
