package io.zephyr.kernel.server;

import io.zephyr.api.CommandContext;
import io.zephyr.api.Invoker;
import io.zephyr.api.Parameters;
import io.zephyr.kernel.command.DaggerShellInjectionConfiguration;
import io.zephyr.kernel.command.DefaultCommandContext;
import io.zephyr.kernel.launch.KernelOptions;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ZephyrServerTest {

  private Server server;
  private Invoker invoker;
  private KernelOptions options;
  private DefaultCommandContext context;

  @BeforeEach
  void setUp() throws RemoteException {

    LocateRegistry.createRegistry(9999);

    options = new KernelOptions();
    options.setPort(9999);
    context = new DefaultCommandContext(null);

    invoker =
        DaggerShellInjectionConfiguration.factory()
            .create(ClassLoader.getSystemClassLoader(), context)
            .createShell();
    server = DaggerServerInjectionConfiguration.factory().build(options, invoker).server();
    context.register(Server.class, server);
  }

  @Test
  void startingServerWorks() throws Exception {
    doStart();

    Invoker localInvoker = (Invoker) LocateRegistry.getRegistry(9999).lookup("ZephyrShell");
    localInvoker.invoke(Parameters.of("server", "stop"));
    assertFalse(server.isRunning());
  }

  private void doStart() throws InterruptedException {
    val t1 = new Thread(() -> server.start());
    t1.start();
    while (!server.isRunning()) {
      Thread.sleep(100);
    }
  }
}
