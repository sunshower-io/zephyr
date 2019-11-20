package io.zephyr.kernel.launch;

import lombok.val;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/** helper utilities for RMI */
public class RMI {

  public static Registry getRegistry(KernelOptions options) {
    val port = options.getPort();
    try {
      return LocateRegistry.getRegistry(port);
    } catch (RemoteException e) {
    }
    try {
      return LocateRegistry.createRegistry(port);
    } catch (RemoteException e) {
    }
    throw new IllegalStateException("Cannot locate or create registry");
  }
}
