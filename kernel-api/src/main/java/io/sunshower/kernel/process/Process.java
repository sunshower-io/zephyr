package io.sunshower.kernel.process;

import io.sunshower.kernel.events.EventSource;
import io.sunshower.kernel.status.StatusAware;
import java.util.concurrent.Callable;

public interface Process<E, T>
    extends Callable<T>, PhaseAware<E, T>, EventSource<E, T>, StatusAware {}
