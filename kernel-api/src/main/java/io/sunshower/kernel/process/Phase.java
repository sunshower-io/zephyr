package io.sunshower.kernel.process;

import io.sunshower.kernel.events.EventSource;

public interface Phase<E, T> extends PhaseAware<E, T>, EventSource<E, T> {

  void execute(Process<E, T> process, T context);
}
