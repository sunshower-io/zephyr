package io.sunshower.kernel.fs;

import static org.junit.jupiter.api.Assertions.*;

import io.sunshower.kernel.core.SunshowerKernel;
import io.sunshower.kernel.launch.KernelOptions;
import io.sunshower.test.common.Tests;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.nio.file.*;
import java.util.Collections;
import java.util.HashMap;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.JUnitTestContainsTooManyAsserts"})
class ModuleFileSystemProviderTest {

  private static KernelOptions kernelOptions;

  @BeforeAll
  static void setUp() {
    val tempDir = Tests.createTemp("temp-dir");
    kernelOptions = new KernelOptions();
    kernelOptions.setHomeDirectory(tempDir);
    SunshowerKernel.setKernelOptions(kernelOptions);
  }

  @Test
  void ensureFileSystemForKernelIsDropletFS() throws IOException {
    val uri = URI.create("droplet://kernel");
    FileSystems.newFileSystem(uri, Collections.emptyMap());
    val fs = FileSystems.getFileSystem(uri);
    try {
      assertTrue(
          fs instanceof ModuleFileSystem, "Filesystem must be an instance of module filesystem");
    } finally {
      fs.close();
    }
  }

  @Test
  void ensureFilesystemReturnedbySubPathIsKernelForKernelPath() throws IOException {
    val uri = URI.create("droplet://kernel/kernel.idx");
    FileSystems.newFileSystem(uri, Collections.emptyMap());
    val fs = FileSystems.getFileSystem(uri);
    try {
      assertSame(
          fs, FileSystems.getFileSystem(URI.create("droplet://kernel")), "Files must be the same");
    } finally {
      fs.close();
    }
  }

  @Test
  void ensureFileSystemCanBeCreated() throws IOException {
    val env = new HashMap<String, Object>();
    val filesystem = FileSystems.newFileSystem(URI.create("droplet://kernel"), env);
    try {
      assertNotNull(filesystem, "Created filesystem must not be null");
    } finally {
      filesystem.close();
    }
  }

  @Test
  void ensureFileSystemCanOnlyBeCreatedOnce() throws IOException {
    val env = new HashMap<String, Object>();
    val fs = Tests.createTemp("sunshower-home");
    env.put(ModuleFileSystem.SUNSHOWER_HOME, fs);
    val filesystem = FileSystems.newFileSystem(URI.create("droplet://kernel"), env);
    try {
      assertThrows(
          FileSystemAlreadyExistsException.class,
          () -> {
            FileSystems.newFileSystem(URI.create("droplet://kernel"), env);
          });
    } finally {
      filesystem.close();
    }
  }

  @Test
  void ensureWritingFileWorks() throws IOException {
    val uri = URI.create("droplet://kernel");
    val fs = FileSystems.newFileSystem(uri, Collections.emptyMap());
    val file = fs.getPath("test.txt").toFile();
    assertTrue(file.getParentFile().mkdirs(), "must be able to create parent");
    assertTrue(file.createNewFile(), "must be able to create file");
    Writer fos = Files.newBufferedWriter(file.toPath(), StandardOpenOption.WRITE);
    fos.write(10);
    fos.close();
    fs.close();
  }
}
