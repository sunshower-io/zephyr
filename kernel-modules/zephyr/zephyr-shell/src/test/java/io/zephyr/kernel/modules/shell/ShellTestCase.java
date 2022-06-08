package io.zephyr.kernel.modules.shell;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.sunshower.lang.events.EventListener;
import io.sunshower.test.common.Tests;
import io.zephyr.api.ModuleEvents;
import io.zephyr.kernel.Lifecycle;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.core.KernelLifecycle.State;
import io.zephyr.kernel.core.ModuleManager;
import io.zephyr.kernel.launch.KernelLauncher;
import io.zephyr.kernel.modules.shell.server.Server;
import java.io.File;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

@Log
public class ShellTestCase {

  protected final boolean installBase;
  protected File homeDirectory;
  protected Kernel kernel;
  protected Server server;
  protected KernelLauncher launcher;

  protected ShellTestCase(final boolean installBase) {
    this.installBase = installBase;
  }

  protected ShellTestCase() {
    this(true);
  }

  @BeforeEach
  protected void setUp() {
    homeDirectory = Tests.createTemp(UUID.randomUUID().toString());
    if (installBase) {
      startServer();
      startKernel();
      installKernelModules(StandardModules.YAML);
      restart();
      assertNotNull(kernel, "Kernel should not be null, but it was");
      assertTrue(
          kernel.getModuleManager().getModules().isEmpty(),
          String.format(
              "Tests must have no plugins installed, but the following plugins were detected: %s",
              kernel.getModuleManager().getModules().stream()
                  .map(t -> t.getCoordinate().toCanonicalForm())
                  .collect(Collectors.toList())));
    }
  }

  @AfterEach
  protected void tearDown() {
    if (installBase) {
      stopKernel();
      stopServer();
    }
    assertSame(kernel.getLifecycle().getState(), State.Stopped);
  }

  protected void restart() {
    stop();
    start();
  }

  protected void start() {
    startServer();
    startKernel();
  }

  protected void stop() {
    try {
      removeAll();
    } finally {
      stopKernel();
      stopServer();
    }
  }

  protected void removeAll() {
    remove(
        kernel.getModuleManager().getModules().stream()
            .map(t -> t.getCoordinate())
            .map(t -> t.toCanonicalForm())
            .collect(Collectors.joining(" ")));
  }

  protected Module moduleNamed(String name) {
    log.log(Level.INFO, "Attempting to locate any module named: {0}", name);
    log.log(Level.INFO, "Available modules: ");

    if (kernel == null) {
      throw new IllegalStateException("Error: kernel is null");
    }
    val manager = kernel.getModuleManager();
    val modules = manager.getModules();
    for (val module : modules) {
      log.log(Level.SEVERE, "\t ''{0}''", module.getCoordinate().getName());
    }

    try {
      await()
          .atMost(10, TimeUnit.SECONDS)
          .until(
              () ->
                  manager.getModules().stream()
                      .anyMatch(t -> t.getCoordinate().getName().contains(name)));
    } catch (Exception ex) {
      log.log(Level.SEVERE, "No module named ''{0}'' found.  Available modules:");
      logModules(manager);
      throw ex;
    }
    return manager.getModules().stream()
        .filter(t -> t.getCoordinate().getName().contains(name))
        .findFirst()
        .get();
  }

  private void logModules(ModuleManager manager) {
    for (val module : manager.getModules()) {
      log.log(
          Level.SEVERE,
          "\t ''{0}''.  State: {1}",
          new Object[] {module.getCoordinate().getName(), module.getLifecycle().getState()});
    }
  }

  protected void startPlugins(String... plugins) {
    val args = new StringBuilder();
    args.append("plugin").append(" start ");
    for (val pluginName : plugins) {
      args.append(moduleNamed(pluginName).getCoordinate().toCanonicalForm()).append(" ");
    }
    runAsync(args.toString().split("\\s+"));
  }

  @SneakyThrows
  protected void startAndWait(int expectedCount, String... plugins) {
    startPlugins(plugins);
    try {
      await()
          .atMost(10, TimeUnit.SECONDS)
          .until(
              () ->
                  kernel.getModuleManager().getModules(Lifecycle.State.Active).size()
                      == expectedCount);
    } catch (Exception ex) {
      logModules(kernel.getModuleManager());
      throw ex;
    }
  }

  @SneakyThrows
  protected void startServer() {
    val serverThread =
        new Thread(
            () -> {
              run("-s -c 4 -h " + homeDirectory.getAbsolutePath());
            });
    serverThread.start();
    //    runAsync("-s", "-c", "4", "-h", homeDirectory.getAbsolutePath());
    await().atMost(10, TimeUnit.SECONDS).until(() -> (KernelLauncher.getInstance() != null));

    launcher = KernelLauncher.getInstance();

    await().atMost(10, TimeUnit.SECONDS).until(() -> launcher.resolveService(Server.class) != null);
    server = launcher.resolveService(Server.class);

    await().atMost(10, TimeUnit.SECONDS).until(() -> server.isRunning());
  }

  @SneakyThrows
  protected void restartKernel() {
    stopKernel();
    startKernel();
  }

  protected void runAsync(String... args) {
    new Thread(
            () -> {
              run(args);
            })
        .start();
  }

  @SneakyThrows
  protected void startKernel() {
    if (server == null) {
      startServer();
    }
    runAsync("kernel", "start", "-h", homeDirectory.getAbsolutePath());
    await()
        .atMost(100, TimeUnit.SECONDS)
        .until(() -> launcher.resolveService(Kernel.class) != null);

    kernel = launcher.resolveService(Kernel.class);
    await()
        .atMost(10, TimeUnit.SECONDS)
        .until(() -> kernel.getLifecycle().getState() == State.Running);
    System.out.println("Successfully started kernel");
    assertEquals(
        kernel.getFileSystem().getRootDirectories().iterator().next().toFile().getAbsoluteFile(),
        new File(homeDirectory, "kernel"));
  }

  @SneakyThrows
  protected void stopKernel() {
    checkServer();
    runAsync("kernel", "stop");
    if (kernel != null) {
      await()
          .atMost(10, TimeUnit.SECONDS)
          .until(() -> kernel.getLifecycle().getState() == State.Stopped);
    }
    System.out.println("Kernel stopped");
  }

  @SneakyThrows
  protected void stopServer() {
    checkServer();
    runAsync("server", "stop");
    await().atMost(10, TimeUnit.SECONDS).until(() -> !server.isRunning());
    System.out.println("Stopped server");
    launcher = null;
  }

  protected void run(String... args) {
    KernelLauncher.main(args);
  }

  protected void installFully(Installable... modules) {
    start();
    install(modules);
    stop();
  }

  @SneakyThrows
  protected void install(Installable... modules) {
    checkServer();
    val result = new StringBuilder("plugin install ");
    for (val module : modules) {
      System.out.println("Installing " + module);
      result.append(module.getAssembly()).append(" ");
      System.out.println("Module installation enqueued");
    }
    val args = result.toString().trim().split("\\s+");
    run(args);
  }

  @SneakyThrows
  protected void remove(String... coordinates) {
    checkServer();
    val result = new StringBuilder("plugin remove ");
    for (val module : coordinates) {
      System.out.println("Removing " + module);
      result.append(module).append(" ");
    }
    run(result.toString().trim().split("\\s+"));
  }

  @SneakyThrows
  protected void installAndWaitForModuleCount(int count, Installable... modules) {

    val failedCount = new AtomicInteger(0);
    EventListener<Object> listener;
    kernel.addEventListener(
        listener = (type, event) -> failedCount.incrementAndGet(), ModuleEvents.INSTALL_FAILED);

    try {
      install(modules);

      await()
          .atMost(100, TimeUnit.SECONDS)
          .until(
              () ->
                  kernel.getModuleManager().getModules().size()
                          + kernel.getKernelModules().size()
                          + failedCount.get()
                      >= count);
    } finally {
      kernel.removeEventListener(listener);
    }
  }

  protected void installKernelModules(Installable... modules) {
    install(modules);
    restartKernel();
  }

  protected int moduleCount() {
    return kernel.getModuleManager().getModules().size();
  }

  protected int kernelModuleCount() {
    return kernel.getKernelModules().size();
  }

  private void checkServer() {
    if (server == null) {
      throw new IllegalStateException("Error:  Server is not running");
    }
  }

  public enum TestPlugins implements Installable {
    TEST_PLUGIN_1("kernel-tests:test-plugins:test-plugin-1"),
    TEST_PLUGIN_2("kernel-tests:test-plugins:test-plugin-2"),
    TEST_PLUGIN_3("kernel-tests:test-plugins:test-plugin-3"),
    TEST_PLUGIN_SPRING("kernel-tests:test-plugins:test-plugin-spring"),
    TEST_PLUGIN_SPRING_DEP("kernel-tests:test-plugins:test-plugin-spring-dep");

    final String path;

    TestPlugins(String path) {
      this.path = path;
    }

    @Override
    public String getPath() {
      return path;
    }
  }

  public enum StandardModules implements Installable {
    YAML("kernel-modules:sunshower-yaml-reader");
    final String path;

    StandardModules(String path) {
      this.path = path;
    }

    @Override
    public String getPath() {
      return path;
    }
  }

  public interface Installable {

    String getPath();

    default String getAssembly() {
      return Tests.relativeToProjectBuild(getPath(), "war", "libs").getAbsolutePath();
    }
  }

  @AllArgsConstructor
  public static final class FileInstallable implements Installable {

    final File file;

    @Override
    public String getPath() {
      return file.getAbsolutePath();
    }

    @Override
    public String getAssembly() {
      return getPath();
    }
  }
}
