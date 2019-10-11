package io.sunshower.kernel.osgi;

import static io.sunshower.common.io.FileNames.nameOf;

import io.sunshower.common.io.MonitorableFileTransfer;
import io.sunshower.kernel.*;
import io.sunshower.kernel.core.DefaultKernelModuleDescriptor;
import io.sunshower.kernel.io.ChannelTransferListener;
import io.sunshower.kernel.launch.KernelOptions;
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
      ExecutorService service,
      KernelOptions options) {
    super(manager, source, target, callable, service, options);
  }

  @Override
  protected KernelModuleDescriptor extract(@NonNull KernelModuleLoadTask task) {
    val file = task.getLoadedFile();
    val directory = task.getExtensionDirectory();
    val moduleDataDir = options.getKernelModuleDataDirectory();
    val dataDirectory = new File(moduleDataDir.toFile().getAbsoluteFile(), nameOf(file.getName()));
    return new DefaultKernelModuleDescriptor(
        task.getSource(), file.toPath(), directory.toPath(), dataDirectory.toPath());
  }
}
