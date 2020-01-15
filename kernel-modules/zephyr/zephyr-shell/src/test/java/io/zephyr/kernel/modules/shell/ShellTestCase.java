package io.zephyr.kernel.modules.shell;

import io.sunshower.test.common.Tests;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.extensions.EntryPoint;
import io.zephyr.kernel.launch.KernelLauncher;
import io.zephyr.kernel.modules.shell.server.Server;
import lombok.SneakyThrows;
import lombok.val;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ExecutionException;

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
      System.out.println(launcher.getEntryPoints());
      Thread.sleep(100);
    }
    while (!server.isRunning()) {
      Thread.sleep(100);
    }
  }

  protected void restartKernel() {
    run("kernel", "restart");
  }
  @SneakyThrows
  protected void startKernel() {
    if (server == null) {
      startServer();
    }
    run("kernel", "start", "-h", homeDirectory.getAbsolutePath());
    while ((kernel = launcher.resolveService(Kernel.class)) == null) {
      Thread.sleep(100);
    }
  }

  protected void stopKernel() {
    checkServer();
    run("kernel", "stop");
  }

  @SneakyThrows
  protected void stopServer() {
    checkServer();
    run("server", "stop");
    launcher = null;
  }

  protected void run(String... args) {
    new Thread(
            () -> {
              KernelLauncher.main(args);
            })
        .start();
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
