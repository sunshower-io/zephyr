package io.sunshower.kernel.modules.readers;

import static io.sunshower.test.common.Tests.projectOutput;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.sunshower.kernel.ext.PluginDescriptorReader;
import java.io.IOException;
import java.util.jar.JarFile;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Note--sometimes you may have to regenerate the jar artifact to get this test to run correctly in
 * some IDEs (gradle clean build)
 */
@Disabled
class YamlPluginDescriptorReaderTest {

  private JarFile pluginFile;
  private PluginDescriptorReader reader;

  @BeforeEach
  void setUp() throws IOException {
    reader = new YamlPluginDescriptorReader();
    val file = projectOutput(":kernel-modules:sunshower-yaml-reader", "jar");
    pluginFile = new JarFile(file);
  }

  @Test
  void ensureFileIsRead() {
    val result = reader.read(pluginFile);
    assertTrue(result.isPresent());
  }

  @Test
  void ensureCoordinateIsCorrect() {
    val result = reader.read(pluginFile).get();
    //    assertEquals(new Coordinate("io.sunshower", "yaml-loader", "1.0.0"), result.getCoordinate());
  }

  @Test
  void ensureDependenciesAreCorrect() {
    //    val result = reader.read(pluginFile).get();
    //    assertEquals(result.getDependencies().size(), 1);
    //    val dep = result.getDependencies().get(0);
    //    assertTrue(dep.isRequired());
    //    assertEquals(new Coordinate("io.sunshower.whatever", "whatever", "1.0.0"),
    // dep.getCoordinate());
  }
}
