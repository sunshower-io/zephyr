package io.sunshower.kernel.modules.readers;

import io.sunshower.kernel.PluginDescriptor;
import io.sunshower.kernel.ext.PluginDescriptorReader;

import java.util.Optional;
import java.util.jar.JarFile;

public class YamlPluginDescriptorReader implements PluginDescriptorReader {

    @Override
    public Optional<PluginDescriptor> read(JarFile file) {
        return null;
    }
}
