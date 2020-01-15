package io.zephyr.kernel.modules.shell.server;

import io.zephyr.kernel.log.Logging;
import io.zephyr.kernel.modules.shell.RMI;
import io.zephyr.kernel.modules.shell.ShellOptions;
import io.zephyr.kernel.modules.shell.console.Invoker;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.val;

@SuppressWarnings({"PMD.UnusedPrivateMethod", "PMD.AvoidUsingVolatile"})
public class ZephyrServer implements Server {

  static final Logger log = Logging.get(ZephyrServer.class);
  private volatile boolean running;
  private final Invoker invoker;
  private final ShellOptions options;

  private final Map<Object, Object> exported;

  public ZephyrServer(ShellOptions options, Invoker invoker) {
    this.options = options;
    this.invoker = invoker;
    exported = new HashMap<>();
  }

  @Override
  public void start() {

    val port = options.getPort();
    log.log(Level.INFO, "zephyr.server.starting", port);
    try {
      log.log(Level.INFO, "zephyr.server.invoker.binding");
      val stub = UnicastRemoteObject.exportObject(invoker, port);
      exported.put(stub, invoker);
      RMI.getRegistry(options).rebind("ZephyrShell", stub);
      log.log(Level.INFO, "zephyr.server.invoker.bound");
      running = true;
      loop(port);
    } catch (RemoteException | InterruptedException e) {
      log.log(Level.WARNING, "Encountered exception", e);
    }
  }

  private void loop(int port) throws InterruptedException {
    log.log(Level.INFO, "zephyr.server.started", port);
    synchronized (invoker) {
      while (running) {
        invoker.wait();
      }
      log.info("zephyr.server.stopped");

      try {
        unregisterCommands();
      } catch (Exception ex) {
        log.log(Level.WARNING, "Encountered exception", ex);
      }
    }
  }

  private void unregisterCommands() throws Exception {
    val registry = RMI.getRegistry(options);
    log.log(Level.INFO, "zephyr.server.unregistering.services");
    try {
      for (val name : registry.list()) {
        log.log(Level.INFO, "zephyr.server.unregistering.service", name);
        try {
          val stub = registry.lookup(name);
          registry.unbind(name);

          val result = exported.get(stub);
          if (result != null) {
            UnicastRemoteObject.unexportObject((Remote) result, true);
          }
        } catch (NoSuchObjectException ex) {
          log.log(Level.INFO, "failed to unregister service");
        }
        log.log(Level.INFO, "zephyr.server.unregistered.service", name);
      }
    } finally {
      try {
        UnicastRemoteObject.unexportObject(registry, true);
      } catch (NoSuchObjectException ex) {
        log.log(Level.INFO, "Server not running");
      }
    }
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  @Override
  public void stop() {
    synchronized (invoker) {
      log.info("zephyr.server.stopping");
      running = false;
      invoker.notifyAll();
    }
  }
}
