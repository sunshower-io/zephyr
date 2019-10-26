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

  public static final String INSTALLED_MODULE = "MODULE_INSTALLED_MODULE";

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
    Assembly assembly = context.getContextValue(ModuleUnpackPhase.MODULE_ASSEMBLY);
    Set<Library> libraries = context.getContextValue(ModuleUnpackPhase.INSTALLED_LIBRARIES);

    DefaultModule module =
        new DefaultModule(
            descriptor.getType(),
            source,
            assembly,
            moduleDirectory.getAbsoluteFile().toPath(),
            descriptor.getCoordinate(),
            fileSystem,
            libraries,
            Set.copyOf(descriptor.getDependencies()));

    val lifecycle = createLifecycle(module);
    module.setLifecycle(lifecycle);
    context.getKernel().getModuleManager().install(module);

    context.setContextValue(INSTALLED_MODULE, module);
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
