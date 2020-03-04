package io.zephyr.scan;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import io.sunshower.kernel.core.ProjectPlugins;
import io.sunshower.kernel.test.KernelTestConfiguration;
import io.sunshower.kernel.test.ZephyrTest;
import io.zephyr.api.ModuleEvents;
import io.zephyr.common.io.Files;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.events.EventListener;
import io.zephyr.kernel.extensions.EntryPoint;
import java.io.File;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import javax.inject.Inject;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.mockito.Mock;
import org.springframework.test.context.ContextConfiguration;

@ZephyrTest
@ContextConfiguration(
  classes = {KernelTestConfiguration.class, DirectoryScannerTestConfiguration.class}
)
class DirectoryScannerTest {

  /** beans */
  @Inject private Kernel kernel;

  @Inject private DirectoryScanner scanner;
  @Inject private ExecutorService executorService;
  @Inject private Map<EntryPoint.ContextEntries, Object> context;

  /** mocks */
  @Mock private EventListener<?> eventListener;

  @AfterEach
  void tearDown() {
    context.remove(EntryPoint.ContextEntries.ARGS);
  }

  @Test
  void ensureOptionsAreExtractedCorrectly() {
    context.put(EntryPoint.ContextEntries.ARGS, new String[] {"-w=hello,world"});
    scanner.initialize(context);
    val options = scanner.getOptions();
    val directories = options.getDirectories();
    assertEquals(2, directories.length);
  }

  @Test
  @SneakyThrows
  @EnabledIfSystemProperty(named = "flakytests", matches = "true")
  void ensureKernelDefaultDeploymentDirectoryIsScannedForRemovals() {
    val file = createFile();
    kernel.addEventListener(eventListener, ModuleEvents.REMOVED, ModuleEvents.INSTALLED);

    context.put(EntryPoint.ContextEntries.ARGS, new String[] {"--scan"});
    scanner.initialize(context);
    val latch = new CyclicBarrier(1);
    watch(latch);
    val installable = ProjectPlugins.TEST_PLUGIN_1.getFile();
    latch.await();
    latch.reset();
    executorService.submit(
        () -> {
          try {
            do {
              Thread.sleep(150);
            } while (!scanner.running);
            Files.transferTo(installable, new File(file, "whatever.war"));
            latch.await();
          } catch (Exception ex) {
            fail();
          }
        });
    latch.await();
    latch.reset();
    verify(eventListener, timeout(10000)).onEvent(eq(ModuleEvents.REMOVED), any());
    reset(eventListener);
    executorService.submit(
        () -> {
          try {
            System.out.println("deleting");
            Thread.sleep(150);
            java.nio.file.Files.delete(new File(file, "whatever.war").getAbsoluteFile().toPath());
          } catch (Exception ex) {

          }
        });
    scanner.stop();
    verify(eventListener, timeout(10000)).onEvent(eq(ModuleEvents.REMOVED), any());
  }

  @Test
  @SneakyThrows
  void ensureKernelDefaultDeploymentDirectoryIsScanned() {
    val file = createFile();
    kernel.addEventListener(eventListener, ModuleEvents.INSTALLING);
    context.put(EntryPoint.ContextEntries.ARGS, new String[] {"--scan"});
    scanner.initialize(context);
    val latch = new CyclicBarrier(1);
    watch(latch);
    val installable = ProjectPlugins.TEST_PLUGIN_1.getFile();
    latch.await();
    latch.reset();
    executorService.submit(
        () -> {
          try {
            do {
              Thread.sleep(150);
            } while (!scanner.running);
            Files.transferTo(installable, new File(file, "whatever.war"));
            latch.await();
          } catch (Exception ex) {
            fail();
          }
        });
    latch.await();

    verify(eventListener, timeout(10000)).onEvent(eq(ModuleEvents.INSTALLING), any());
    scanner.stop();
  }

  private void watch(CyclicBarrier latch) {
    executorService.submit(
        () -> {
          scanner.initialize(context);
          scanner.run(context);
          try {
            latch.await();
          } catch (Exception ex) {
            fail();
          }
        });
  }

  private File createFile() {
    val path = kernel.getFileSystem().getPath("deployments");
    val file = path.toAbsolutePath().toFile();
    if (!file.mkdirs()) {
      if (!file.exists()) {
        throw new IllegalStateException("Failed to create file");
      }
    }
    return file;
  }
}
