package command;
//
//import static org.junit.jupiter.api.Assertions.assertFalse;
//
//import io.sunshower.test.common.Tests;
//import io.zephyr.cli.Invoker;
//import io.zephyr.cli.Parameters;
//import io.zephyr.kernel.command.DaggerShellInjectionConfiguration;
//import io.zephyr.kernel.command.DefaultCommandContext;
//import io.zephyr.kernel.launch.KernelOptions;
//import io.zephyr.kernel.launch.RMI;
//import java.rmi.RemoteException;
//import java.rmi.registry.LocateRegistry;
//import lombok.val;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;

@SuppressWarnings({
  "PMD.DoNotUseThreads",
  "PMD.EmptyCatchBlock",
  "PMD.JUnitTestsShouldIncludeAssert",
  "PMD.JUnitTestContainsTooManyAsserts"
})
class ZephyrServerTest {

  //  private Server server;
  //  private Invoker invoker;
  //  private KernelOptions options;
  //  private DefaultCommandContext context;
  //
  //  @BeforeEach
  //  void setUp() throws RemoteException {
  //
  //    try {
  //      LocateRegistry.createRegistry(9999);
  //    } catch (RemoteException ex) {
  //
  //    }
  //    options = new KernelOptions();
  //    options.setHomeDirectory(Tests.createTemp());
  //    options.setPort(9999);
  //    context = new DefaultCommandContext();
  //    RMI.getRegistry(options);
  //
  //    invoker =
  //        DaggerShellInjectionConfiguration.factory()
  //            .create(ClassLoader.getSystemClassLoader(), context)
  //            .createShell();
  //    server = DaggerServerInjectionConfiguration.factory().build(options, invoker).server();
  //    context.register(Server.class, server);
  //    context.register(KernelOptions.class, options);
  //  }
  //
  //  @AfterEach
  //  void tearDown() {
  //    if (server.isRunning()) {
  //      server.stop();
  //    }
  //  }
  //
  //  @Test
  //  void startingServerWorks() throws Exception {
  //    doStart();
  //
  //    Invoker localInvoker = (Invoker) LocateRegistry.getRegistry(9999).lookup("ZephyrShell");
  //    localInvoker.invoke(Parameters.of("server", "stop"));
  //    assertFalse(server.isRunning(), "server must not be running after stop");
  //  }
  //
  //  @Test
  //  void ensureStartingKernelWorks() throws Exception {
  //    doStart();
  //    Invoker localInvoker = (Invoker) LocateRegistry.getRegistry(9999).lookup("ZephyrShell");
  //    localInvoker.invoke(Parameters.of("kernel", "start"));
  //  }
  //
  //  private void doStart() throws InterruptedException {
  //    val t1 = new Thread(() -> server.start());
  //    t1.start();
  //    while (!server.isRunning()) {
  //      Thread.sleep(100);
  //    }
  //  }
}
