package io.zephyr.kernel.core.actions;

import io.sunshower.gyre.Scope;
import io.zephyr.api.ModuleEvents;
import io.zephyr.kernel.KernelModuleEntry;
import io.zephyr.kernel.Library;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.concurrency.Task;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.core.KernelException;
import io.zephyr.kernel.events.Events;
import io.zephyr.kernel.log.Logging;
import io.zephyr.kernel.misc.SuppressFBWarnings;
import io.zephyr.kernel.module.ModuleListParser;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.val;

@SuppressWarnings("PMD.UnusedPrivateMethod")
public class WriteKernelModuleListPhase extends Task {

  static final Logger log = Logging.get(WriteKernelModuleListPhase.class);

  public WriteKernelModuleListPhase(String name) {
    super(name);
  }

  @Override
  public TaskValue run(Scope scope) {
    Set<Module> descriptors = scope.get(ModuleInstallationCompletionPhase.INSTALLED_KERNEL_MODULES);
    if (descriptors == null || descriptors.isEmpty()) {
      log.info("no descriptors found");
      return null;
    }
    log.log(Level.INFO, "located {0} modules to install", descriptors.size());

    val kernel = scope.<Kernel>get("SunshowerKernel");
    val fs = kernel.getFileSystem();
    Set<KernelModuleEntry> entries = readEntries(fs);

    val file = fs.getPath("modules.list");
    writeModules(kernel, file, descriptors, entries);

    return null;
  }

  private Set<KernelModuleEntry> readEntries(FileSystem fs) {
    return new HashSet<>(ModuleListParser.read(fs, "modules.list"));
  }

  @SuppressFBWarnings
  @SuppressWarnings({"PMD.AvoidInstantiatingObjectsInLoops", "PMD.UnusedPrivateMethod"})
  private void writeModules(
      Kernel kernel, Path file, Collection<Module> modules, Set<KernelModuleEntry> entries) {
    try (val outputStream =
        new BufferedWriter(
            new OutputStreamWriter(
                Files.newOutputStream(
                    file,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.DSYNC,
                    StandardOpenOption.APPEND)))) {
      for (val module : modules) {
        val coord = module.getCoordinate();
        val entry =
            new KernelModuleEntry(
                module.getOrder(),
                coord.getName(),
                coord.getGroup(),
                coord.getVersion().toString(),
                libraryFiles(module, module.getLibraries()));
        if (!entries.contains(entry)) {
          outputStream.write(entry.toString());
          outputStream.write("\n");
          entries.add(entry);
          kernel.dispatchEvent(ModuleEvents.INSTALLED, Events.create(entry));
        }
      }
    } catch (IOException ex) {
      throw new KernelException(ex);
    }
  }

  @SuppressWarnings({"PMD.UnusedPrivateMethod"})
  private List<String> libraryFiles(Module module, Collection<Library> libraries) {
    List<String> result = new ArrayList<>(libraries.size());
    for (val library : libraries) {
      result.add(library.getFile().getAbsolutePath());
    }
    result.add(module.getAssembly().getFile().getAbsolutePath());
    return result;
  }
}
