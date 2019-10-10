package io.sunshower.kernel.osgi;

import io.sunshower.common.io.MonitorableFileTransfer;
import io.sunshower.kernel.*;
import io.sunshower.kernel.core.DefaultKernelModuleDescriptor;
import io.sunshower.kernel.io.ChannelTransferListener;
import java.io.File;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import lombok.NonNull;
import lombok.val;

public class DefaultModuleLoadTask
    extends AbstractKernelExtensionLoadTask<KernelModuleDescriptor, KernelModuleLoadTask>
    implements ChannelTransferListener, KernelModuleLoadTask {

  @SuppressWarnings("unchecked")
  public DefaultModuleLoadTask(
      KernelModuleManager manager,
      URL source,
      File target,
      MonitorableFileTransfer callable,
      ExecutorService service) {
    super(manager, source, target, callable, service);
  }

  @Override
  protected KernelModuleDescriptor extract(@NonNull KernelModuleLoadTask task) {
    val file = task.getLoadedFile();
    val directory = task.getExtensionDirectory();
    return new DefaultKernelModuleDescriptor(task.getSource(), file.toPath(), directory.toPath());
  }
}
