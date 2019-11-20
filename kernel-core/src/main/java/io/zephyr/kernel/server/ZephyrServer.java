package io.zephyr.kernel.server;

import io.zephyr.api.Invoker;
import io.zephyr.kernel.launch.KernelOptions;
import io.zephyr.kernel.launch.RMI;
import io.zephyr.kernel.log.Logging;
import lombok.val;

import java.rmi.AlreadyBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ZephyrServer implements Server {

  static final Logger log = Logging.get(ZephyrServer.class);
  private volatile boolean running;
  private final Invoker invoker;
  private final KernelOptions options;

  public ZephyrServer(KernelOptions options, Invoker invoker) {
    this.options = options;
    this.invoker = invoker;
  }

  @Override
  public void start() {

    val port = options.getPort();
    log.log(Level.INFO, "zephyr.server.starting", port);
    try {
      log.log(Level.INFO, "zephyr.server.invoker.binding");
      val stub = UnicastRemoteObject.exportObject((Remote) invoker, port);
      RMI.getRegistry(options).bind("ZephyrShell", stub);
      log.log(Level.INFO, "zephyr.server.invoker.bound");
      running = true;
      loop(port);
    } catch (RemoteException | InterruptedException | AlreadyBoundException e) {
      e.printStackTrace();
    }
  }

  private void loop(int port) throws InterruptedException {
    log.log(Level.INFO, "zephyr.server.started", port);
    synchronized (invoker) {
      while (running) {
        invoker.wait();
      }
      log.info("zephyr.server.stopped");
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
      invoker.notify();
    }
  }
}
