package command;

import static org.junit.jupiter.api.Assertions.assertFalse;

import io.sunshower.test.common.Tests;
import io.zephyr.kernel.launch.KernelOptions;
import io.zephyr.kernel.modules.shell.RMI;
import io.zephyr.kernel.modules.shell.ShellOptions;
import io.zephyr.kernel.modules.shell.command.DaggerShellInjectionConfiguration;
import io.zephyr.kernel.modules.shell.command.DefaultCommandContext;
import io.zephyr.kernel.modules.shell.console.Invoker;
import io.zephyr.kernel.modules.shell.console.Parameters;
import io.zephyr.kernel.modules.shell.server.DaggerServerInjectionConfiguration;
import io.zephyr.kernel.modules.shell.server.Server;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Collections;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings({
  "PMD.DoNotUseThreads",
  "PMD.EmptyCatchBlock",
  "PMD.JUnitTestsShouldIncludeAssert",
  "PMD.JUnitTestContainsTooManyAsserts"
})
class ZephyrServerTest {

  private Server server;
  private Invoker invoker;
  private ShellOptions options;
  private DefaultCommandContext context;
  private KernelOptions kernelOptions;

  @BeforeEach
  void setUp() throws RemoteException {

    try {
      LocateRegistry.createRegistry(9999);
    } catch (RemoteException ex) {

    }
    options = new ShellOptions();
    kernelOptions = new KernelOptions();

    kernelOptions.setHomeDirectory(Tests.createTemp());

    options.setPort(9999);
    context = new DefaultCommandContext(Collections.emptyMap());
    RMI.getRegistry(options);

    invoker =
        DaggerShellInjectionConfiguration.factory()
            .create(ClassLoader.getSystemClassLoader(), context)
            .createShell();
    server = DaggerServerInjectionConfiguration.factory().build(options, invoker).server();
    context.register(Server.class, server);
    context.register(ShellOptions.class, options);
    context.register(KernelOptions.class, kernelOptions);
  }

  @AfterEach
  void tearDown() {
    if (server.isRunning()) {
      server.stop();
    }
  }

  @Test
  void startingServerWorks() throws Exception {
    doStart();

    Invoker localInvoker = (Invoker) LocateRegistry.getRegistry(9999).lookup("ZephyrShell");
    localInvoker.invoke(Parameters.of("server", "stop"));
    assertFalse(server.isRunning(), "server must not be running after stop");
  }

  @Test
  void ensureStartingKernelWorks() throws Exception {
    doStart();
    Invoker localInvoker = (Invoker) LocateRegistry.getRegistry(9999).lookup("ZephyrShell");
    localInvoker.invoke(
        Parameters.of("kernel", "start", "-h", Tests.createTemp().getAbsolutePath()));
    localInvoker.invoke(
        Parameters.of("kernel", "stop", "-h", Tests.createTemp().getAbsolutePath()));
  }

  private void doStart() throws InterruptedException {
    val t1 = new Thread(() -> server.start());
    t1.start();
    while (!server.isRunning()) {
      Thread.sleep(100);
    }
  }
}
