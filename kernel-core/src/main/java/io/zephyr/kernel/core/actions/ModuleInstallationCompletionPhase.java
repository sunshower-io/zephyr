package io.zephyr.kernel.core.actions;

import io.sunshower.gyre.Scope;
import io.zephyr.kernel.*;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.concurrency.Task;
import io.zephyr.kernel.core.*;
import io.zephyr.kernel.events.Events;
import io.zephyr.kernel.module.ModuleLifecycle;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.util.Set;
import lombok.val;

public class ModuleInstallationCompletionPhase extends Task {

  public static final String INSTALLED_PLUGINS = "INSTALLED_PLUGINS";
  public static final String INSTALLED_KERNEL_MODULES = "INSTALLED_KERNEL_MODULES";

  public ModuleInstallationCompletionPhase(String name) {
    super(name);
  }

  @Override
  @SuppressWarnings({"PMD.CloseResource"})
  public TaskValue run(Scope context) {
    synchronized (this) {
      Kernel           kernel          = context.<Kernel>get("SunshowerKernel");
      URL              url             = context.get(ModuleDownloadPhase.DOWNLOAD_URL);
      Source           source          = new ModuleSource(getSource(url));
      ModuleDescriptor descriptor      = context.get(ModuleScanPhase.MODULE_DESCRIPTOR);
      FileSystem       fileSystem      = context.get(ModuleTransferPhase.MODULE_FILE_SYSTEM);
      File             moduleDirectory = context.get(ModuleTransferPhase.MODULE_DIRECTORY);
      Assembly         assembly        = context.get(ModuleUnpackPhase.MODULE_ASSEMBLY);
      Set<Library>     libraries       = context.get(ModuleUnpackPhase.INSTALLED_LIBRARIES);

      DefaultModule module =
          new DefaultModule(
              descriptor.getOrder(),
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

      if (descriptor.getType() == Module.Type.Plugin) {
        context.<Set<Module>>get(INSTALLED_PLUGINS).add(module);
        kernel.dispatchEvent(PluginEvents.PLUGIN_INSTALLATION_COMPLETE, Events.create(module));
      } else {
        context.<Set<Module>>get(INSTALLED_KERNEL_MODULES).add(module);
      }
      return null;
    }
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