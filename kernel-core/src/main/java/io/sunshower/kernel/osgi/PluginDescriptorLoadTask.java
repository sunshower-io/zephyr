package io.sunshower.kernel.osgi;

import static io.sunshower.common.io.FileNames.nameOf;

import io.sunshower.common.io.MonitorableFileTransfer;
import io.sunshower.kernel.*;
import io.sunshower.kernel.core.DefaultPluginDescriptor;
import io.sunshower.kernel.io.ChannelTransferListener;
import io.sunshower.kernel.launch.KernelOptions;
import java.io.File;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import lombok.val;

public class PluginDescriptorLoadTask
    extends AbstractKernelExtensionLoadTask<PluginDescriptor, PluginLoadTask>
    implements ChannelTransferListener, PluginLoadTask {

  @SuppressWarnings("unchecked")
  public PluginDescriptorLoadTask(
      PluginManager manager,
      URL source,
      File target,
      MonitorableFileTransfer callable,
      ExecutorService service,
      KernelOptions options) {
    super(manager, source, target, callable, service, options);
  }

  @Override
  protected PluginDescriptor extract(PluginLoadTask task) {
    val file = task.getLoadedFile();
    val directory = task.getExtensionDirectory();
    val pluginDataDir = options.getPluginDataDirectory();
    val dataDirectory = new File(pluginDataDir.toFile().getAbsoluteFile(), nameOf(file.getName()));
    return new DefaultPluginDescriptor(
        task.getSource(), file.toPath(), directory.toPath(), dataDirectory.toPath());
  }
}
