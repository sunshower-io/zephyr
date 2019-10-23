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
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.JUnitTestContainsTooManyAsserts"})
class ModuleFileSystemProviderTest {
  static final Logger log = Logger.getLogger(ModuleFileSystemProviderTest.class.getName());

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

  @Test
  void ensureWritingToFullURIWorks() throws IOException {
    val uri = URI.create("droplet://kernel");
    val fs = FileSystems.newFileSystem(uri, Collections.emptyMap());
    try {
      val path = fs.provider().getPath(URI.create("droplet://kernel/hello/frapper.txt"));
      val file = path.toFile();
      assertFalse(file.exists(), "file must not initially exist");
      if (!file.getParentFile().mkdirs()) {
        log.log(Level.WARNING, "File {0} already exists", file.getParentFile().toPath());
      }
      assertTrue(file.createNewFile(), "must be able to create a new file");
    } finally {
      fs.close();
    }
  }

  @Test
  void ensureWritingToURIInRootWorks() throws IOException {
    val uri = URI.create("droplet://kernel");
    val fs = FileSystems.newFileSystem(uri, Collections.emptyMap());
    try {
      val path = fs.provider().getPath(URI.create("droplet://kernel/frap.txt"));
      val file = path.toFile();
      if (!file.getParentFile().mkdirs()) {
        log.log(Level.WARNING, "File {0} already exists", file.getParentFile().toPath());
      }
      assertFalse(file.exists(), "file must not exist");
      assertTrue(file.createNewFile(), "must be able to create file");

    } finally {
      fs.close();
    }
  }

  @Test
  void ensureRetrievingFileSystemForVersionedPathProducesVersionedModuleFileSystem()
      throws IOException {
    val uri = URI.create("droplet://sunshower.test-plugin/1.0.0-SNAPSHOT");
    val fs = FileSystems.newFileSystem(uri, Collections.emptyMap());
    try {
      assertTrue(
          fs instanceof VersionedModuleFileSystem,
          "Filesystem must be a versioned module filesystem");
    } finally {
      fs.close();
    }
  }
}
