package io.sunshower.kernel.core.actions;

import io.sunshower.gyre.Scope;
import io.sunshower.kernel.Library;
import io.sunshower.kernel.Module;
import io.sunshower.kernel.concurrency.Task;
import io.sunshower.kernel.core.Kernel;
import io.sunshower.kernel.core.KernelException;
import io.sunshower.kernel.core.ModuleDescriptor;
import io.sunshower.kernel.log.Logging;
import io.sunshower.kernel.misc.SuppressFBWarnings;
import io.sunshower.kernel.module.KernelModuleEntry;
import lombok.val;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    val file = kernel.getFileSystem().getPath("modules.list");
    writeModule(file, descriptors);

    return null;
  }

  @SuppressFBWarnings
  private void writeModule(Path file, Collection<Module> modules) {
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
        outputStream.write(entry.toString());
        outputStream.write("\n");
      }
    } catch (IOException ex) {
      throw new KernelException(ex);
    }
  }

  private List<String> libraryFiles(Module module, Collection<Library> libraries) {
    List<String> result = new ArrayList<>(libraries.size());
    for (val library : libraries) {
      result.add(library.getFile().getAbsolutePath());
    }
    result.add(module.getAssembly().getFile().getAbsolutePath());
    return result;
  }

  //    List<Module> module = scope.get(ModuleInstallationCompletionPhase.INSTALLED_MODULE);
  //    if (module.getType() == Module.Type.KernelModule) {
  //      log.log(Level.INFO, "module.type.kernel.begin", module.getCoordinate());
  //      //      val fileSystem = FileSystems.getFileSystem(URI.create("droplet://kernel"));
  //      //      val path = fileSystem.getPath(KernelModuleEntry.MODULE_LIST);
  //      //      //      context.getKernel().scheduleTask(new ModuleEntryWrite(path, module));
  //    } else {
  //      log.log(Level.INFO, "module.type.plugin", module.getCoordinate());
}
