package io.zephyr.kernel.module;

import io.zephyr.kernel.Assembly;
import io.zephyr.kernel.Library;
import io.zephyr.kernel.concurrency.TaskException;
import io.zephyr.kernel.concurrency.TaskStatus;
import io.zephyr.kernel.extensions.ModuleAssemblyExtractor;
import io.zephyr.kernel.misc.SuppressFBWarnings;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import lombok.val;

public abstract class AbstractCompressedModuleAssemblyExtractor implements ModuleAssemblyExtractor {

  @Override
  public void extract(Assembly assembly, FileSystem moduleFilesystem, ExtractionListener listener)
      throws Exception {

    val compressedAssembly = createArchive(assembly.getFile());
    doExtract(compressedAssembly, getLibraryDirectories(), moduleFilesystem, assembly, listener);
  }

  protected ZipFile createArchive(File file) throws IOException {
    return new ZipFile(file);
  }

  /** @return the library directories to search through (e.g. WEB-INF/lib/, BOOT-INF/lib) */
  protected abstract Collection<String> getLibraryDirectories();

  /**
   * @param name
   * @return true if this directory should be included on the classpath (e.g. WEB-INF/classes, etc.)
   */
  protected abstract boolean isResourceDirectory(String name, ZipFile file);

  @SuppressWarnings({"PMD.DataflowAnomalyAnalysis"})
  protected void doExtract(
      ZipFile compressedAssembly,
      Collection<String> libDirectories,
      FileSystem moduleFileSystem,
      Assembly assembly,
      ExtractionListener listener)
      throws IOException {

    //    val compressedAssembly = new JarFile(assemblyFile, true);
    val entries = compressedAssembly.entries();
    while (entries.hasMoreElements()) {
      val next = entries.nextElement();
      val name = next.getName();
      if (isResourceDirectory(name, compressedAssembly)) {
        assembly.addSubpath(name);
      }
      for (val libdir : libDirectories) {
        if (next.isDirectory()) {
          continue;
        }
        if (name.startsWith(libdir)) {
          unpackDirectory(
              moduleFileSystem, compressedAssembly, next, libdir, name, assembly, listener);
        }
      }
    }
  }

  protected void unpackDirectory(
      FileSystem moduleFileSystem,
      ZipFile compressedAssembly,
      ZipEntry next,
      String libdir,
      String name,
      Assembly assembly,
      ExtractionListener listener)
      throws IOException {
    val dirname = dirname(libdir);
    val path = moduleFileSystem.getPath(dirname).toFile();
    if (!path.exists()) {
      if (!path.mkdirs()) {
        throw new TaskException(TaskStatus.UNRECOVERABLE);
      }
    }
    doTransfer(compressedAssembly, next, name, path, assembly, listener);
  }

  @SuppressFBWarnings
  @SuppressWarnings("PMD.UnusedPrivateMethod")
  private void doTransfer(
      ZipFile compressedAssembly,
      ZipEntry next,
      String name,
      File path,
      Assembly assembly,
      ExtractionListener listener)
      throws IOException {
    val target = new File(path, getFileName(name));
    try (val inputStream = compressedAssembly.getInputStream(next)) {
      listener.beforeEntryExtracted(name, target);
      java.nio.file.Files.copy(inputStream, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
      assembly.addLibrary(new Library(target));
      listener.afterEntryExtracted(name, target);
    }
  }

  protected static String dirname(String libdir) {
    val normalized = libdir.substring(0, libdir.length() - 1);
    return normalized.substring(normalized.lastIndexOf('/') + 1);
  }

  public static String getFileName(String name) {
    val sepidx = name.lastIndexOf(File.separator);
    if (sepidx > 0) {
      return name.substring(sepidx + 1);
    }
    return name;
  }
}
