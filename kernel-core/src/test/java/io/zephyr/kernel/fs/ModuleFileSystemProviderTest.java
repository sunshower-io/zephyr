package io.zephyr.kernel.fs;

import static org.junit.jupiter.api.Assertions.*;

import io.sunshower.test.common.Tests;
import io.zephyr.kernel.core.SunshowerKernel;
import io.zephyr.kernel.launch.KernelOptions;
import io.zephyr.kernel.misc.SuppressFBWarnings;
import java.io.IOException;
import java.net.URI;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

@SuppressFBWarnings
@DisabledOnOs(OS.WINDOWS)
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
  void ensureIsSameFileWorks() throws IOException {
    val uri = URI.create("droplet://kernel");
    val fs = FileSystems.newFileSystem(uri, Collections.emptyMap());
    try {
      Files.createFile(fs.getPath("test.txt"));
      assertTrue(
          fs.provider().isSameFile(fs.getPath("test.txt"), fs.getPath("test.txt")),
          "same files must be the same");
    } finally {
      try {
        Files.deleteIfExists(fs.getPath("test.txt"));
      } finally {
        fs.close();
      }
    }
  }

  @Test
  void ensureDifferentFilesAreNotTheSame() throws IOException {
    val uri = URI.create("droplet://kernel");
    val fs = FileSystems.newFileSystem(uri, Collections.emptyMap());
    try {

      Files.createFile(fs.getPath("a.txt"));
      Files.createFile(fs.getPath("b.text"));
      assertFalse(
          fs.provider().isSameFile(fs.getPath("a.txt"), fs.getPath("b.text")),
          "different files reported as the same");
    } finally {
      try {
        Files.deleteIfExists(fs.getPath("a.txt"));
        Files.deleteIfExists(fs.getPath("b.text"));
      } finally {
        fs.close();
      }
    }
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
    try {
      val file = fs.getPath("test.txt").toFile();
      assertTrue(file.createNewFile(), "must be able to create file");
      try (val fos = Files.newBufferedWriter(file.toPath(), StandardOpenOption.WRITE)) {
        fos.write(10);
      }
    } finally {
      Files.deleteIfExists(fs.getPath("test.txt"));
      fs.close();
    }
  }

  @Test
  void ensureWritingToFullURIWorks() throws IOException {
    val uri = URI.create("droplet://kernel");
    val fs = FileSystems.newFileSystem(uri, Collections.emptyMap());
    try {
      val path = fs.provider().getPath(URI.create("droplet://kernel/hello/frapper.txt"));
      val file = path.toFile();
      if (!file.exists()) {
        assertFalse(file.exists(), "file must not initially exist");
      }
      if (!file.getParentFile().mkdirs()) {
        log.log(Level.WARNING, "File {0} already exists", file.getParentFile().toPath());
      }
      if (!file.exists()) {
        assertTrue(file.createNewFile(), "must be able to create a new file");
      }
    } finally {
      fs.close();
    }
  }

  @Test
  void ensureFileSystemIsCreatedWithVersionedPath() throws IOException {
    val uri = URI.create("droplet://com.sunshower-test/test.txt?version=1.0.0");
    val fs = (ModuleFileSystem) FileSystems.newFileSystem(uri, Collections.emptyMap());
    try {
      assertEquals(fs.key.length, 3, "Key must be correct");
    } finally {
      fs.close();
    }
  }

  @Test
  void ensureFileSystemWithVersionedPathWorks() throws IOException {
    val uri = URI.create("droplet://com.sunshower-test/test.txt?version=1.0.0");
    val fs = (ModuleFileSystem) FileSystems.newFileSystem(uri, Collections.emptyMap());
    try {
      val f = fs.getPath("test.txt");
      assertTrue(f.toFile().createNewFile(), "file must be creatable");
      try (val w = Files.newBufferedWriter(f)) {
        w.write(10);
        w.flush();
      }
    } finally {
      Files.deleteIfExists(fs.getPath("test.txt"));
      fs.close();
    }
  }

  @Test
  void ensureFileSystemWithVersionedSubPathWorks() throws IOException {
    val uri = URI.create("droplet://com.sunshower-test/test.txt?version=1.0.0");
    val fs = (ModuleFileSystem) FileSystems.newFileSystem(uri, Collections.emptyMap());
    try {
      Path f = fs.getPath("test", "test.txt");
      // TODO josiah fix
      if (!f.getParent().toFile().exists()) {
        assertTrue(f.getParent().toFile().mkdirs(), "parents must be creatable");
      }
      if (!f.toFile().exists()) {
        assertTrue(f.toFile().createNewFile(), "file must be creatable");
      }
      try (val w = Files.newBufferedWriter(f)) {
        w.write(10);
        w.flush();
      }
    } finally {
      Files.deleteIfExists(fs.getPath("test.txt"));
      fs.close();
    }
  }

  @Test
  void ensureWritingToURIInRootWorks() throws IOException {
    val uri = URI.create("droplet://com.sunshower-test/test.txt?version=1.0.0");
    val fs = FileSystems.newFileSystem(uri, Collections.emptyMap());
    try {
      val path =
          fs.provider().getPath(URI.create("droplet://com.sunshower-test/frap.txt?version=1.0.0"));
      val file = path.toFile();
      if (!file.getParentFile().mkdirs()) {
        log.log(Level.WARNING, "File {0} already exists", file.getParentFile().toPath());
      }
      if (!file.exists()) {
        assertFalse(file.exists(), "file must not exist");
        assertTrue(file.createNewFile(), "must be able to create file");
      }
    } finally {
      val path =
          fs.provider().getPath(URI.create("droplet://com.sunshower-test/frap.txt?version=1.0.0"));
      Files.deleteIfExists(path);
      fs.close();
    }
  }

  @Test
  void ensureNewByteChannelWorksOnDropletFS() throws IOException, InterruptedException {
    val uri = URI.create("droplet://com.sunshower-test/test.txt?version=1.0.0");
    val fs = FileSystems.newFileSystem(uri, Collections.emptyMap());
    val toWrite = "Hello world";
    try (val channel =
        fs.provider()
            .newOutputStream(
                Paths.get("test.txt"),
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING)) {
      channel.write(toWrite.getBytes(StandardCharsets.UTF_8));
      channel.flush();
      val str = Files.readString(fs.getPath("test.txt"));
      assertEquals(toWrite, str, "must be equal");
    } finally {

      deleteFile(fs.getPath("test.txt"));
      fs.close();
    }
  }

  @SneakyThrows
  private void deleteFile(Path path) {
    Files.deleteIfExists(path);
  }

  @Test
  void ensureNewByteChannelWorksOnKernel() throws IOException {
    val uri = URI.create("droplet://kernel");
    val fs = FileSystems.newFileSystem(uri, Collections.emptyMap());
    val toWrite = CharBuffer.wrap("Hello world");
    try {
      val channel =
          (FileChannel)
              fs.provider()
                  .newByteChannel(
                      Paths.get("test.txt"),
                      EnumSet.of(
                          StandardOpenOption.CREATE,
                          StandardOpenOption.WRITE,
                          StandardOpenOption.READ,
                          StandardOpenOption.TRUNCATE_EXISTING));

      val buffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, toWrite.length());
      buffer.put(StandardCharsets.UTF_8.encode(toWrite));
      buffer.force();
      channel.force(true);

      val str = Files.readString(fs.getPath("test.txt"));
      assertEquals("Hello world", str, "Must be equal");

    } finally {
      try {
        Files.deleteIfExists(fs.getPath("test.txt"));
      } catch (IOException ex) {
        log.log(
            Level.WARNING,
            "Failed to delete " + fs.getPath("test.txt") + " reason: " + ex.getMessage());
      }
      fs.close();
    }
  }
}
