package io.sunshower.kernel.modules.readers;

import com.esotericsoftware.yamlbeans.YamlReader;
import io.sunshower.kernel.PluginDescriptor;
import io.sunshower.kernel.PluginLoadException;
import io.sunshower.kernel.ext.PluginDescriptorReader;
import io.sunshower.kernel.modules.descriptors.PluginHolder;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.jar.JarFile;
import lombok.val;

public class YamlPluginDescriptorReader implements PluginDescriptorReader {

  @Override
  public Optional<PluginDescriptor> read(JarFile file) {
    val entry = file.getEntry("META-INF/plugin/plugin.yaml");
    if (entry == null) {
      return Optional.empty();
    }

    try {
      val inputStream = file.getInputStream(entry);
      val yaml = new YamlReader(new InputStreamReader(inputStream));
      return Optional.of(yaml.read(PluginHolder.class).plugin);
    } catch (IOException ex) {
      ex.printStackTrace();
      throw new PluginLoadException("yaml plugin manifest exists, but could not be read");
    }
  }
}
