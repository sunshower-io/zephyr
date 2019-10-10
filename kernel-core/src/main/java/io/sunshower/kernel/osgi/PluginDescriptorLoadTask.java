package io.sunshower.kernel.osgi;

import io.sunshower.common.io.MonitorableFileTransfer;
import io.sunshower.kernel.*;
import io.sunshower.kernel.core.DefaultPluginDescriptor;
import io.sunshower.kernel.io.ChannelTransferListener;
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
      ExecutorService service) {
    super(manager, source, target, callable, service);
  }

  @Override
  protected PluginDescriptor extract(PluginLoadTask task) {
    val file = task.getLoadedFile();
    val directory = task.getExtensionDirectory();
    return new DefaultPluginDescriptor(task.getSource(), file.toPath(), directory.toPath());
  }
}
