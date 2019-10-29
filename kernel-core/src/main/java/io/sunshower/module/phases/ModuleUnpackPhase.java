package io.sunshower.module.phases;

import io.sunshower.common.io.Files;
import io.sunshower.kernel.Assembly;
import io.sunshower.kernel.Library;
import io.sunshower.kernel.log.Logger;
import io.sunshower.kernel.log.Logging;
import io.sunshower.kernel.misc.SuppressFBWarnings;
import io.sunshower.kernel.process.*;
import io.sunshower.kernel.process.Process;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import lombok.val;

@SuppressWarnings("PMD.UnusedPrivateMethod")
public class ModuleUnpackPhase extends AbstractPhase<KernelProcessEvent, KernelProcessContext> {

  static final Logger log;
  static final ResourceBundle bundle;

  public static final String LIBRARY_DIRECTORIES = "MODULE_UNPACK_LIBRARIES";
  public static final String INSTALLED_LIBRARIES = "MODULE_INSTALLED_LIBRARIES";
  public static final String MODULE_ASSEMBLY = "MODULE_RELEVANT_SEARCH_PATHS";
  private static final Set<String> RESOURCE_DIRECTORIES = Set.of("WEB-INF/classes/");

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
    File assemblyFile = context.getContextValue(ModuleTransferPhase.MODULE_ASSEMBLY_FILE);
    val assembly = new Assembly(assemblyFile);
    val libraryFiles = new HashSet<Library>();
    FileSystem moduleFileSystem = context.getContextValue(ModuleTransferPhase.MODULE_FILE_SYSTEM);

    try {
      log.log(Level.INFO, "module.unpack.begin", assemblyFile);
      doExtract(libDirectories, assemblyFile, moduleFileSystem, libraryFiles, assembly);
      log.log(Level.INFO, "module.unpack.complete", assemblyFile);
      context.setContextValue(MODULE_ASSEMBLY, assembly);
      context.setContextValue(INSTALLED_LIBRARIES, libraryFiles);
    } catch (IOException ex) {
      log.log(Level.WARNING, "module.unpack.failed", assemblyFile);
      throw new PhaseException(State.Unrecoverable, this, ex);
    }
  }

  @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
  private void doExtract(
      Set<String> libDirectories,
      File assemblyFile,
      FileSystem moduleFileSystem,
      Set<Library> libraryFiles,
      Assembly assembly)
      throws IOException {

    val compressedAssembly = new JarFile(assemblyFile, true);
    val entries = compressedAssembly.entries();
    while (entries.hasMoreElements()) {
      val next = entries.nextElement();
      val name = next.getName();
      if (isResourceDirectory(name)) {
        assembly.addSubpath(name);
      }
      for (val libdir : libDirectories) {
        if (next.isDirectory()) {
          continue;
        }
        if (name.startsWith(libdir)) {
          unpackDirectory(moduleFileSystem, compressedAssembly, next, libdir, name, libraryFiles);
        }
      }
    }
  }

  private boolean isResourceDirectory(String name) {
    return RESOURCE_DIRECTORIES.contains(name);
  }

  private void unpackDirectory(
      FileSystem moduleFileSystem,
      JarFile compressedAssembly,
      JarEntry next,
      String libdir,
      String name,
      Set<Library> libraryFiles)
      throws IOException {
    val dirname = dirname(libdir);
    val path = moduleFileSystem.getPath(dirname).toFile();
    if (!path.exists()) {
      if (!path.mkdirs()) {
        throw new PhaseException(State.Unrecoverable, this, "unable to make lib directory");
      }
    }
    doTransfer(compressedAssembly, next, name, path, libraryFiles);
  }

  @SuppressFBWarnings
  private void doTransfer(
      JarFile compressedAssembly, JarEntry next, String name, File path, Set<Library> libraryFiles)
      throws IOException {
    val target = new File(path, Files.getFileName(name));
    try (val inputStream = compressedAssembly.getInputStream(next)) {
      if (log.isLoggable(Level.FINE)) {
        log.log(Level.FINE, "module.unpack.file", new Object[] {name, target});
      }
      java.nio.file.Files.copy(inputStream, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
      libraryFiles.add(new Library(target));
      if (log.isLoggable(Level.FINE)) {
        log.log(Level.FINE, "module.unpack.file.complete", new Object[] {name, target});
      }
    }
  }

  private String dirname(String libdir) {
    val normalized = libdir.substring(0, libdir.length() - 1);
    return normalized.substring(normalized.lastIndexOf('/') + 1);
  }
}
