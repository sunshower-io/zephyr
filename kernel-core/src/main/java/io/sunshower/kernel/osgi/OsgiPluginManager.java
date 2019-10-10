package io.sunshower.kernel.osgi;

import io.sunshower.common.io.MonitorableFileTransfer;
import io.sunshower.kernel.*;
import io.sunshower.kernel.common.i18n.Localization;
import io.sunshower.kernel.launch.KernelOptions;
import java.io.File;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OsgiPluginManager
    extends AbstractKernelExtensionManager<PluginDescriptor, PluginLoadTask>
    implements PluginManager {

  public OsgiPluginManager(
      KernelOptions options, OsgiEnabledKernel kernel, Localization localization) {
    super(options, kernel, localization);
  }

  @Override
  protected PluginDescriptorLoadTask create(
      URL url, File destination, MonitorableFileTransfer callable, ExecutorService service) {
    return new PluginDescriptorLoadTask(this, url, destination, callable, service);
  }
}
