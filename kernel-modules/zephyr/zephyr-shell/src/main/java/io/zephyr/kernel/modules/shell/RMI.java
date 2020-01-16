package io.zephyr.kernel.modules.shell;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import lombok.extern.java.Log;
import lombok.val;

/** helper utilities for RMI */
@Log
public class RMI {

  public static Registry getRegistry(ShellOptions options) {
    val port = options.getPort();
    try {
      return LocateRegistry.getRegistry(port);
    } catch (RemoteException e) {
      log.log(Level.WARNING, "Encountered exception while trying to run command", e.getMessage());
    }
    try {
      return LocateRegistry.createRegistry(port);
    } catch (RemoteException e) {
      log.log(Level.WARNING, "Encountered exception while trying to run command", e.getMessage());
    }
    throw new IllegalStateException("Cannot locate or create registry");
  }
}
