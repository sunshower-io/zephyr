package io.sunshower.kernel.ext.scanner;

import static org.junit.jupiter.api.Assertions.*;

import io.sunshower.kernel.core.ModuleScanner;
import io.sunshower.test.common.Tests;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ServiceLoader;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class YamlPluginDescriptorScannerTest {

  private File moduleFile;
  private URL fileUrl;
  private ModuleScanner scanner;

  @BeforeEach
  void setUp() throws MalformedURLException {
    scanner = ServiceLoader.load(ModuleScanner.class).findFirst().get();
    moduleFile = Tests.relativeToCurrentProjectBuild("war", "libs");
    fileUrl = moduleFile.toURI().toURL();
  }

  @Test
  void ensureReadingPluginNameWorks() {
    val moduleDescriptor = scanner.scan(moduleFile, fileUrl).get();
    assertEquals(moduleDescriptor.getCoordinate().getName(), "yaml-loader", "name must be correct");
  }
}
