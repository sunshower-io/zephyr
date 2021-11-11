package io.zephyr.kernel.core.actions;

import io.sunshower.gyre.Scope;
import io.zephyr.api.ModuleEvents;
import io.zephyr.kernel.Assembly;
import io.zephyr.kernel.Dependency;
import io.zephyr.kernel.Lifecycle;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.Source;
import io.zephyr.kernel.concurrency.Task;
import io.zephyr.kernel.core.DefaultModule;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.core.ModuleDescriptor;
import io.zephyr.kernel.core.ModuleSource;
import io.zephyr.kernel.events.Events;
import io.zephyr.kernel.module.ModuleLifecycle;
import io.zephyr.kernel.status.StatusType;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.val;

@SuppressWarnings("PMD.UnusedPrivateMethod")
public class ModuleInstallationCompletionPhase extends Task {

  public static final String INSTALLED_PLUGINS = "INSTALLED_PLUGINS";
  public static final String INSTALLED_KERNEL_MODULES = "INSTALLED_KERNEL_MODULES";

  public ModuleInstallationCompletionPhase(String name) {
    super(name);
  }

  @Override
  @SuppressWarnings({"PMD.CloseResource", "PMD.DataflowAnomalyAnalysis"})
  public TaskValue run(Scope context) {
    synchronized (this) {
      final Kernel kernel = context.get("SunshowerKernel");
      final URL url = context.get(ModuleDownloadPhase.DOWNLOAD_URL);
      final Source source = new ModuleSource(getSource(url));
      final ModuleDescriptor descriptor = context.get(ModuleScanPhase.MODULE_DESCRIPTOR);
      final FileSystem fileSystem = context.get(ModuleTransferPhase.MODULE_FILE_SYSTEM);
      final File moduleDirectory = context.get(ModuleTransferPhase.MODULE_DIRECTORY);
      final Assembly assembly = context.get(ModuleUnpackPhase.MODULE_ASSEMBLY);
      //      final Set<Library> libraries = context.get(ModuleUnpackPhase.INSTALLED_LIBRARIES);

      val module =
          new DefaultModule(
              descriptor.getOrder(),
              descriptor.getType(),
              source,
              kernel,
              assembly,
              moduleDirectory.getAbsoluteFile().toPath(),
              descriptor.getCoordinate(),
              fileSystem,
              assembly.getLibraries(),
              getDependencies(descriptor.getDependencies()));

      val lifecycle = createLifecycle(module);
      module.setLifecycle(lifecycle);

      if (descriptor.getType() == Module.Type.Plugin) {
        context.<Set<Module>>get(INSTALLED_PLUGINS).add(module);
      } else {
        context.<Set<Module>>get(INSTALLED_KERNEL_MODULES).add(module);
      }
      kernel.dispatchEvent(
          ModuleEvents.INSTALLING,
          Events.create(
              module,
              StatusType.SUCCEEDED.resolvable("Successfully installed plugin: " + descriptor)));
      return null;
    }
  }

  private Set<Dependency> getDependencies(List<Dependency> dependencies) {
    val results = new ArrayList<>(dependencies);
    results.sort(Dependency.orderComparator());
    return new LinkedHashSet<>(results);
  }

  private Lifecycle createLifecycle(Module module) {
    val lifecycle = new ModuleLifecycle(module);
    lifecycle.setState(Lifecycle.State.Uninstalled);
    return lifecycle;
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
