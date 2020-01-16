package io.zephyr.kernel.modules.shell;

import io.sunshower.test.common.Tests;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.core.KernelLifecycle;
import io.zephyr.kernel.extensions.EntryPoint;
import io.zephyr.kernel.launch.KernelLauncher;
import io.zephyr.kernel.modules.shell.server.Server;
import java.io.File;
import java.util.Map;
import lombok.SneakyThrows;
import lombok.val;

public class ShellTestCase {

  protected File homeDirectory = Tests.createTemp();
  protected Kernel kernel;
  protected Server server;
  protected Thread serverThread;
  protected KernelLauncher launcher;
  protected Map<EntryPoint.ContextEntries, Object> launcherContext;

  public enum StandardModules {
    YAML("kernel-modules:sunshower-yaml-reader");

    final String path;

    StandardModules(String path) {
      this.path = path;
    }

    public String getPath() {
      return Tests.relativeToProjectBuild(path, "war", "libs").getAbsolutePath();
    }
  }

  @SneakyThrows
  protected void startServer() {
    serverThread =
        new Thread(
            () -> {
              run("-s");
            });
    serverThread.start();
    while ((launcher = KernelLauncher.getInstance()) == null) {
      Thread.sleep(100);
    }

    while ((server = launcher.resolveService(Server.class)) == null) {
      Thread.sleep(100);
    }
    while (!server.isRunning()) {
      Thread.sleep(100);
    }
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
    while ((kernel = launcher.resolveService(Kernel.class)) == null) {
      Thread.sleep(100);
    }
    while (kernel.getLifecycle().getState() != KernelLifecycle.State.Running) {
      Thread.sleep(100);
    }
    System.out.println("Successfully started kernel");
  }

  @SneakyThrows
  protected void stopKernel() {
    checkServer();
    runAsync("kernel", "stop");
    while (kernel.getLifecycle().getState() != KernelLifecycle.State.Stopped) {
      Thread.sleep(100);
    }
    System.out.println("Kernel stopped");
  }

  @SneakyThrows
  protected void stopServer() {
    checkServer();
    runAsync("server", "stop");
    while (server.isRunning()) {
      Thread.sleep(100);
    }
    System.out.println("Stopped server");
    launcher = null;
  }

  protected void run(String... args) {
    KernelLauncher.main(args);
  }

  @SneakyThrows
  protected void install(StandardModules... modules) {
    checkServer();
    val result = new StringBuilder("plugin install ");
    for (val module : modules) {
      result.append(module.getPath()).append(" ");
    }
    val args = result.toString().trim().split("\\s+");
    run(args);
  }

  private void checkServer() {
    if (server == null) {
      throw new IllegalStateException("Error:  Server is not running");
    }
  }
}
