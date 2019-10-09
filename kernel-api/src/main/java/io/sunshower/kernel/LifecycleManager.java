package io.sunshower.kernel;

import java.util.concurrent.Future;

public interface LifecycleManager {

    Kernel.State getCurrentState();

    Future<Void> setCurrentState(Kernel.State state);

    Future<Void> stop();

    Future<Void> start();

    Future<Void> restart();

    Future<Void> passivate();



}
