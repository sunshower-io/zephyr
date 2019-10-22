package io.sunshower.kernel.fs;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.FileSystems;
import static org.junit.jupiter.api.Assertions.*;

class ModuleFileSystemProviderTest {

  @Test
  void ensureFileSystemsAreSameInstanceForKernelAndRoot() {
    val whost = URI.create("droplet://kernel");
    val wfs = URI.create("droplet:///");
    assertSame(
        FileSystems.getFileSystem(whost),
        FileSystems.getFileSystem(wfs),
        "Filesystems must refer to the same instance");
  }

  @Test
  void ensureFileSystemForKernelIsDropletFS() {
    val uri = URI.create("droplet://kernel");
    val fs = FileSystems.getFileSystem(uri);
    assertTrue(
        fs instanceof ModuleFileSystem, "Filesystem must be an instance of module filesystem");
  }

  @Test
  void ensureFileSystemForSchemeIsDropletFS() {
    val uri = URI.create("droplet:///");
    val fs = FileSystems.getFileSystem(uri);
    assertTrue(
        fs instanceof ModuleFileSystem, "Filesystem must be an instance of module filesystem");
  }
}
