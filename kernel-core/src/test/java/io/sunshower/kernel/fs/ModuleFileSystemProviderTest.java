package io.sunshower.kernel.fs;

import io.sunshower.kernel.core.SunshowerKernel;
import io.sunshower.kernel.launch.KernelOptions;
import io.sunshower.test.common.Tests;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystems;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class ModuleFileSystemProviderTest {

  private static KernelOptions kernelOptions;

  @BeforeAll
  public static void setUp() {
    val tempDir = Tests.createTemp("temp-dir");
    kernelOptions = new KernelOptions();
    kernelOptions.setHomeDirectory(tempDir);
    SunshowerKernel.setKernelOptions(kernelOptions);
  }

  @Test
  void ensureFileSystemForKernelIsDropletFS() {
    val uri = URI.create("droplet://kernel");
    val fs = FileSystems.getFileSystem(uri);
    assertTrue(
        fs instanceof ModuleFileSystem, "Filesystem must be an instance of module filesystem");
  }

  @Test
  void ensureFilesystemReturnedbySubPathIsKernelForKernelPath() {
    val uri = URI.create("droplet://kernel/kernel.idx");
    val fs = FileSystems.getFileSystem(uri);
    assertSame(fs, FileSystems.getFileSystem(URI.create("droplet://kernel")));
  }

  @Test
  void ensureFileSystemCanBeCreated() throws IOException {
    val env = new HashMap<String, Object>();
    val filesystem = FileSystems.newFileSystem(URI.create("droplet://kernel"), env);
    assertNotNull(filesystem, "Created filesystem must not be null");
  }

  @Test
  void ensureFileSystemIsCreatedWithSpecifiedRoot() throws IOException {
    val env = new HashMap<String, Object>();
    val fs = Tests.createTemp("sunshower-home");
    env.put(ModuleFileSystem.SUNSHOWER_HOME, fs);
    FileSystems.newFileSystem(URI.create("droplet://kernel"), env);
    assertThrows(
        FileSystemAlreadyExistsException.class,
        () -> {
          FileSystems.newFileSystem(URI.create("droplet://kernel"), env);
        });
  }
}
