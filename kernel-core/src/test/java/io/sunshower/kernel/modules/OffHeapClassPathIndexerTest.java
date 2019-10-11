package io.sunshower.kernel.modules;

import static io.sunshower.kernel.KernelTests.loadTestModuleFile;
import static io.sunshower.test.common.Tests.createTemp;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.concurrent.Executors;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;

class OffHeapClassPathIndexerTest {

  @Test
  @SneakyThrows
  void ensureReadingWARFileListsAllSubFiles() {
    val km = loadTestModuleFile("sunshower-yaml-reader", "war");
    val indexFile = new File(createTemp("test-idx"), "idx");
    val indexer = new OffHeapClassPathIndexer(indexFile, km, Executors.newWorkStealingPool());
    indexer.index(true);
  }
}
