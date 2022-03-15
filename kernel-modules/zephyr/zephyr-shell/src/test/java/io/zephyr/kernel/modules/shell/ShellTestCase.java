package io.zephyr.kernel.modules.shell;

import static org.awaitility.Awaitility.await;

import io.sunshower.test.common.Tests;
import io.zephyr.api.ModuleEvents;
import io.zephyr.kernel.Lifecycle;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.core.KernelLifecycle.State;
import io.zephyr.kernel.events.EventListener;
import io.zephyr.kernel.extensions.EntryPoint;
import io.zephyr.kernel.launch.KernelLauncher;
import io.zephyr.kernel.modules.shell.server.Server;
import java.io.File;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.parallel.Isolated;

@DisabledIfEnvironmentVariable(
    named = "BUILD_ENVIRONMENT",
    matches = "github",
    disabledReason = "RMI is flaky")
@Isolated
public class ShellTestCase {

  static volatile int count;
  protected final boolean installBase;
  protected File homeDirectory;
  protected Kernel kernel;
  protected Server server;
  protected Thread serverThread;
  protected KernelLauncher launcher;
  protected Map<EntryPoint.ContextEntries, Object> launcherContext;

  protected ShellTestCase(final boolean installBase) {
    this.installBase = installBase;
  }

  protected ShellTestCase() {
    this(true);
  }

  @BeforeEach
  protected void setUp() {
    homeDirectory = Tests.createTemp("test-" + ++count);
    if (installBase) {
      startServer();
      startKernel();
      installKernelModules(StandardModules.YAML);
    }
  }

  @AfterEach
  protected void tearDown() {
    if (installBase) {
      stopKernel();
      stopServer();
    }
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
    stopKernel();
    stopServer();
  }

  protected Module moduleNamed(String name) {
    return kernel.getModuleManager().getModules().stream()
        .filter(t -> t.getCoordinate().getName().equals(name))
        .findAny()
        .orElseThrow(() -> new NoSuchElementException("No plugin named " + name));
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
    while (kernel.getModuleManager().getModules().stream()
            .filter(t -> t.getLifecycle().getState() == Lifecycle.State.Active)
            .count()
        != expectedCount) {
      Thread.sleep(200);
    }
  }

  @SneakyThrows
  protected void startServer() {
    serverThread =
        new Thread(
            () -> {
              run("-s -c 4");
            });
    serverThread.start();
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
    await().atMost(10, TimeUnit.SECONDS).until(() -> launcher.resolveService(Kernel.class) != null);

    kernel = launcher.resolveService(Kernel.class);
    await()
        .atMost(10, TimeUnit.SECONDS)
        .until(() -> kernel.getLifecycle().getState() == State.Running);
    System.out.println("Successfully started kernel");
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
  protected void installAndWaitForModuleCount(int count, Installable... modules) {

    val failedCount = new AtomicInteger(0);
    EventListener<Object> listener;
    kernel.addEventListener(
        listener = (type, event) -> failedCount.incrementAndGet(), ModuleEvents.INSTALL_FAILED);

    try {
      install(modules);

      await()
          .atMost(10, TimeUnit.SECONDS)
          .until(() -> kernel.getModuleManager().getModules().size() + failedCount.get() >= count);
    } finally {
      kernel.removeEventListener(listener);
    }
  }

  protected void installKernelModules(Installable... modules) {
    install(modules);
    restartKernel();
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
