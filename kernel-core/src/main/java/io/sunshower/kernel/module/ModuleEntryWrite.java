package io.sunshower.kernel.module;

import io.sunshower.kernel.Library;
import io.sunshower.kernel.Module;
import io.sunshower.kernel.concurrency.ConcurrentProcess;
import io.sunshower.kernel.core.KernelException;
import io.sunshower.kernel.misc.SuppressFBWarnings;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.val;

public class ModuleEntryWrite implements ConcurrentProcess {
  static final String channel = "kernel:module:module:write";

  final Path file;
  final Module module;

  public ModuleEntryWrite(final Path file, final Module module) {
    this.file = file;
    this.module = module;
  }

  @Override
  public String getChannel() {
    return channel;
  }

  @Override
  @SuppressFBWarnings
  public void perform() {
    try (val outputStream =
        new BufferedWriter(
            new OutputStreamWriter(
                Files.newOutputStream(
                    file,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.DSYNC,
                    StandardOpenOption.APPEND)))) {

      val coord = module.getCoordinate();
      val entry =
          new KernelModuleEntry(
              module.getOrder(),
              coord.getName(),
              coord.getGroup(),
              coord.getVersion().toString(),
              libraryFiles(module.getLibraries()));
      outputStream.write(entry.toString());
      outputStream.write("\n");
    } catch (IOException ex) {
      throw new KernelException(ex);
    }
  }

  private List<String> libraryFiles(Collection<Library> libraries) {
    List<String> result = new ArrayList<>(libraries.size());
    for (val library : libraries) {
      result.add(library.getFile().getAbsolutePath());
    }
    return result;
  }
}
