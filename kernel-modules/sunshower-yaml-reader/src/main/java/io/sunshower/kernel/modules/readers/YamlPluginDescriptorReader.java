package io.sunshower.kernel.modules.readers;

import com.esotericsoftware.yamlbeans.YamlReader;
import io.sunshower.kernel.PluginDescriptor;
import io.sunshower.kernel.PluginLoadException;
import io.sunshower.kernel.ext.PluginDescriptorReader;
import lombok.val;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.jar.JarFile;

public class YamlPluginDescriptorReader implements PluginDescriptorReader {


    @Override
    public Optional<PluginDescriptor> read(JarFile file) {
        val entry = file.getEntry("plugin.yaml");
        if (entry == null) {
            return Optional.empty();
        }

        try {
            val inputStream = file.getInputStream(entry);
            val yaml        = new YamlReader(new InputStreamReader(inputStream));
            return Optional.of(yaml.read(PluginDescriptor.class));
        } catch (IOException ex) {
            throw new PluginLoadException("yaml plugin manifest exists, but could not be read");
        }

    }
}
