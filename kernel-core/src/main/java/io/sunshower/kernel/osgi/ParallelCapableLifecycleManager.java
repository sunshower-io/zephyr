package io.sunshower.kernel.osgi;

import io.sunshower.kernel.Kernel;
import io.sunshower.kernel.LifecycleManager;
import java.util.concurrent.Future;

public class ParallelCapableLifecycleManager implements LifecycleManager {

  @Override
  public Kernel.State getCurrentState() {
    return null;
  }

  @Override
  public Future<Void> setCurrentState(Kernel.State state) {
    return null;
  }

  @Override
  public Future<Void> stop() {
    return null;
  }

  @Override
  public Future<Void> start() {
    return null;
  }

  @Override
  public Future<Void> restart() {
    return null;
  }

  @Override
  public Future<Void> passivate() {
    return null;
  }
}
