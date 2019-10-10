package io.sunshower.kernel.core;

import io.sunshower.kernel.AbstractKernelExtensionDescriptor;
import io.sunshower.kernel.PluginDescriptor;
import java.net.URL;
import java.nio.file.Path;
import lombok.NonNull;

public class DefaultPluginDescriptor extends AbstractKernelExtensionDescriptor
    implements PluginDescriptor {
  public DefaultPluginDescriptor(
      @NonNull URL source, @NonNull Path loadedFile, @NonNull Path loadDirectory) {
    super(source, loadedFile, loadDirectory);
  }
}
