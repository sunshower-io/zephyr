package io.sunshower.kernel.ext;

import io.sunshower.kernel.PluginDescriptor;
import java.util.Optional;
import java.util.jar.JarFile;

public interface PluginDescriptorReader {

  Optional<PluginDescriptor> read(JarFile file);
}
