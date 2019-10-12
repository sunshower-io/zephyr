package io.sunshower.kernel.modules;

import static io.sunshower.kernel.KernelTests.loadTestModuleFile;
import static io.sunshower.test.common.Tests.createTemp;
import static org.junit.jupiter.api.Assertions.*;

import io.sunshower.kernel.KernelModuleManager;
import io.sunshower.kernel.launch.KernelOptions;
import io.sunshower.kernel.osgi.OsgiEnabledKernel;
import java.io.File;
import java.util.concurrent.ExecutorService;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OffHeapClassPathIndexerTest {

  private File storage;
  private KernelOptions options;
  private OsgiEnabledKernel kernel;
  private KernelModuleManager kernelManager;
  private ExecutorService executorService;

  @BeforeEach
  void setUp() {
    options = new KernelOptions();
    storage = createTemp("idx-test");
    options.overrideStorage(storage.getAbsolutePath());
    kernel = new OsgiEnabledKernel(options);
    kernelManager = kernel.getModuleManager();
    executorService = options.getExecutorService();
  }

  @Test
  @SneakyThrows
  void ensureReadingWARFileListsAllSubFiles() {
    val kernelModule = loadTestModuleFile("sunshower-yaml-reader", "war");
    val task = kernelManager.loadExtensionFile(kernelModule.toURI().toURL());
    task.start();
    val descriptor = task.getFuture().get();
    val datDirectory = descriptor.getDataDirectory().toFile();
    val indexer = new OffHeapClassPathIndexer(kernelModule, datDirectory, executorService);
    try (val cl = indexer.index(true)) {}
  }

  @Test
  @SneakyThrows
  void ensureLocatingJarEntryFromIndexWorks() {
    val kernelModule = loadTestModuleFile("sunshower-yaml-reader", "war");
    val task = kernelManager.loadExtensionFile(kernelModule.toURI().toURL());
    task.start();
    val descriptor = task.getFuture().get();
    val datDirectory = descriptor.getDataDirectory().toFile();
    val indexer = new OffHeapClassPathIndexer(kernelModule, datDirectory, executorService);
    try (val classIndex = indexer.index(true)) {
      val entry =
          classIndex.getEntry(
              "com/esotericsoftware/yamlbeans/YamlReader$YamlReaderException.class");
      assertNotNull(entry);
    }
  }
}
