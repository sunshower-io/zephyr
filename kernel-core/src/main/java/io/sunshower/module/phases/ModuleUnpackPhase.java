package io.sunshower.module.phases;

import io.sunshower.common.io.Files;
import io.sunshower.kernel.log.Logging;
import io.sunshower.kernel.process.*;
import io.sunshower.kernel.process.Process;
import lombok.val;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ModuleUnpackPhase extends AbstractPhase<KernelProcessEvent, KernelProcessContext> {

  static final Logger log;
  static final ResourceBundle bundle;

  public static final String LIBRARY_DIRECTORIES = "MODULE_UNPACK_LIBRARIES";

  static {
    log = Logging.get(ModuleUnpackPhase.class);
    bundle = log.getResourceBundle();
  }

  enum EventType implements KernelProcessEvent {}

  public ModuleUnpackPhase() {
    super(EventType.class);
  }

  @Override
  protected void doExecute(
      Process<KernelProcessEvent, KernelProcessContext> process, KernelProcessContext context) {
    Set<String> libDirectories = context.getContextValue(LIBRARY_DIRECTORIES);
    File assemblyFile = context.getContextValue(ModuleTransferPhase.MODULE_ASSEMBLY);
    FileSystem moduleFileSystem = context.getContextValue(ModuleTransferPhase.MODULE_FILE_SYSTEM);

    try {
      log.log(Level.INFO, "module.unpack.begin", assemblyFile);
      doExtract(libDirectories, assemblyFile, moduleFileSystem, context);
      log.log(Level.INFO, "module.unpack.complete", assemblyFile);
    } catch (IOException ex) {
      log.log(Level.WARNING, "module.unpack.failed", assemblyFile);
      throw new PhaseException(State.Unrecoverable, this, ex);
    }
  }

  private void doExtract(
      Set<String> libDirectories,
      File assemblyFile,
      FileSystem moduleFileSystem,
      KernelProcessContext context)
      throws IOException {

    val compressedAssembly = new JarFile(assemblyFile, true);
    val entries = compressedAssembly.entries();
    while (entries.hasMoreElements()) {
      val next = entries.nextElement();
      for (val libdir : libDirectories) {
        if (next.isDirectory()) {
          continue;
        }
        val name = next.getName();
        if (name.startsWith(libdir)) {
          val dirname = dirname(libdir);
          val path = moduleFileSystem.getPath(dirname).toFile();
          if (!path.exists()) {
            if (!path.mkdirs()) {
              throw new PhaseException(State.Unrecoverable, this, "unable to make lib directory");
            }
          }

          val target = new File(path, Files.getFileName(name));
          val inputStream = compressedAssembly.getInputStream(next);
          if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "module.unpack.file", new Object[] {name, target});
          }
          Files.transferTo(target, inputStream);
          if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "module.unpack.file.complete", new Object[] {name, target});
          }
        }
      }
    }
  }

  private String dirname(String libdir) {
    val normalized = libdir.substring(0, libdir.length() - 1);
    return normalized.substring(normalized.lastIndexOf('/') + 1);
  }
}
