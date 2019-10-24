package io.sunshower.module.phases;

import io.sunshower.kernel.*;
import io.sunshower.kernel.Module;
import io.sunshower.kernel.core.ModuleDescriptor;
import io.sunshower.kernel.process.AbstractPhase;
import io.sunshower.kernel.process.KernelProcessContext;
import io.sunshower.kernel.process.KernelProcessEvent;
import io.sunshower.kernel.process.Process;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.util.Set;
import lombok.val;

public class ModuleInstallationCompletionPhase
    extends AbstractPhase<KernelProcessEvent, KernelProcessContext> {

  enum EventType implements KernelProcessEvent {}

  public ModuleInstallationCompletionPhase() {
    super(EventType.class);
  }

  @Override
  protected void doExecute(
      Process<KernelProcessEvent, KernelProcessContext> process, KernelProcessContext context) {
    URL url = context.getContextValue(ModuleDownloadPhase.DOWNLOAD_URL);
    Source source = new ModuleSource(getSource(url));
    ModuleDescriptor descriptor = context.getContextValue(ModuleScanPhase.MODULE_DESCRIPTOR);
    FileSystem fileSystem = context.getContextValue(ModuleTransferPhase.MODULE_FILE_SYSTEM);
    File moduleDirectory = context.getContextValue(ModuleTransferPhase.MODULE_DIRECTORY);
    //    File moduleFile = context.getContextValue(ModuleTransferPhase.MODULE_ASSEMBLY);

    DefaultModule module =
        new DefaultModule(
            descriptor.getType(),
            source,
            moduleDirectory.getAbsoluteFile().toPath(),
            descriptor.getCoordinate(),
            fileSystem,
            Set.copyOf(descriptor.getDependencies()));

    val lifecycle = createLifecycle(module);
    module.setLifecycle(lifecycle);
    context.getKernel().getModuleManager().resolve(module);
  }

  private Lifecycle createLifecycle(Module module) {
    return new ModuleLifecycle(module);
  }

  @SuppressWarnings("PMD.PreserveStackTrace")
  private URI getSource(URL url) {
    try {
      return url.toURI();
    } catch (URISyntaxException e) {
      throw new IllegalStateException("URI was valid before but isn't now??");
    }
  }
}
