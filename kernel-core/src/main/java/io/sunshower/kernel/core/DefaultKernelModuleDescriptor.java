package io.sunshower.kernel.core;

import io.sunshower.kernel.AbstractKernelExtensionDescriptor;
import io.sunshower.kernel.KernelModuleDescriptor;
import java.net.URL;
import java.nio.file.Path;
import lombok.NonNull;

public class DefaultKernelModuleDescriptor extends AbstractKernelExtensionDescriptor
    implements KernelModuleDescriptor {
  public DefaultKernelModuleDescriptor(
      @NonNull URL source,
      @NonNull Path loadedFile,
      @NonNull Path loadDirectory,
      @NonNull Path dataDirectory) {
    super(source, loadedFile, loadDirectory, dataDirectory);
  }
}
