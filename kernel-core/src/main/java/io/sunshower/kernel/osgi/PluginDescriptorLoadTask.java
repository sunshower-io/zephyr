package io.sunshower.kernel.osgi;

import io.sunshower.common.io.MonitorableFileTransfer;
import io.sunshower.kernel.*;
import io.sunshower.kernel.graph.Dependency;
import io.sunshower.kernel.io.ChannelTransferListener;
import lombok.val;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;

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
    val descriptor = new PluginDescriptor() {
      @Override
      public Coordinate getCoordinate() {
        return null;
      }

      @Override
      public List<Dependency> getDependencies() {
        return null;
      }
    };
    manager.register(descriptor);
    return descriptor;
  }
}
