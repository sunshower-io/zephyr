package launch;

import io.sunshower.test.common.Tests;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.core.KernelLifecycle;
import io.zephyr.kernel.core.SunshowerKernel;
import io.zephyr.kernel.launch.KernelLauncher;
import io.zephyr.kernel.launch.KernelOptions;
import io.zephyr.kernel.misc.SuppressFBWarnings;
import io.zephyr.kernel.modules.shell.server.Server;
import java.net.URI;
import java.nio.file.FileSystems;
import java.time.Duration;
import java.util.Collection;
import java.util.function.Predicate;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.AfterEach;

@SuppressWarnings({
  "PMD.DoNotUseThreads",
  "PMD.AvoidUsingVolatile",
  "PMD.DataflowAnomalyAnalysis",
  "PMD.AbstractClassWithoutAbstractMethod"
})
public abstract class CommandTestCase {

  protected volatile KernelLauncher launcher;

  protected Kernel getKernel() {
    if (launcher == null) {
      throw new IllegalStateException("Must start kernel");
    }

    val kernel = launcher.resolveService(Kernel.class);

    if (kernel == null) {
      throw new IllegalStateException("looks like you probably ran doRun() instead of runRemote()");
    }
    return kernel;
  }

  protected Server startServer() {
    runKernel("-s");
    Server server;
    for (; ; ) {
      server = launcher.resolveService(Server.class);
      if (server != null) {
        if (server.isRunning()) {
          break;
        }
      }
    }
    return server;
  }

  protected void runKernel(String... args) {
    val thread = new Thread(() -> KernelLauncher.doLaunch(args));
    thread.start();
    while (KernelLauncher.getInstance() == null) {
      Thread.yield();
    }
    launcher = KernelLauncher.getInstance();
  }

  @SneakyThrows
  protected void waitForPluginCount(int count) {
    waitForKernel();
    val kernel = launcher.resolveService(Kernel.class);
    while (kernel.getModuleManager().getModules().size() != count) {
      Thread.sleep(100);
    }
  }

  @SneakyThrows
  protected void waitForPluginState(Predicate<Collection<Module>> modules) {
    waitForKernel();
    val kernel = launcher.resolveService(Kernel.class);
    for (; ; ) {
      val actual = kernel.getModuleManager().getModules();
      if (modules.test(actual)) {
        return;
      }
      Thread.sleep(50);
    }
  }

  protected void waitForKernelState(KernelLifecycle.State state, Duration duration) {
    if (launcher == null) {
      throw new IllegalStateException("You must call doRun() first");
    }
    long now = System.currentTimeMillis();
    long then = now + duration.toMillis();

    for (; ; ) {
      if (now > then) {
        break;
      }
      now = System.currentTimeMillis();
      val kernel = launcher.resolveService(Kernel.class);
      if (kernel == null) {
        continue;
      }
      val lifecycle = kernel.getLifecycle();
      if (lifecycle.getState() == state) {
        break;
      }
    }
  }

  protected void waitForKernelState(KernelLifecycle.State state) {
    waitForKernelState(state, Duration.ofSeconds(10));
  }

  protected void waitForKernel() {
    waitForKernelState(KernelLifecycle.State.Running);
  }

  @AfterEach
  @SuppressFBWarnings
  @SuppressWarnings("PMD.EmptyCatchBlock")
  protected void tearDown() {
    try {
      val options = new KernelOptions();
      options.setHomeDirectory(Tests.createTemp());
      SunshowerKernel.setKernelOptions(options);
      val fs = FileSystems.getFileSystem(URI.create("droplet://kernel"));
      fs.close();
    } catch (Exception ex) {
      // eh
    }
  }
}
