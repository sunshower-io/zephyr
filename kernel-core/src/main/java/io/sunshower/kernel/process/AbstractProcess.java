package io.sunshower.kernel.process;

import io.sunshower.kernel.events.AbstractEventSource;
import io.sunshower.kernel.events.EventListener;
import java.util.ArrayList;
import java.util.List;
import lombok.val;

public class AbstractProcess<E, T> extends AbstractEventSource<E, T> implements Process<E, T> {
  private final T context;
  private final List<Phase<E, T>> phases;

  protected AbstractProcess(final Class<E> type, final T context) {
    super(type);
    this.context = context;
    phases = new ArrayList<>();
  }

  @Override
  public void addPhase(Phase<E, T> phase) {
    phases.add(phase);
  }

  @Override
  public void removePhase(Phase<E, T> phase) {
    phases.remove(phase);
  }

  @Override
  public void removeListener(E type, EventListener<E, T> listener) {
    super.removeListener(type, listener);
    for (val phase : phases) {
      phase.removeListener(type, listener);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public void registerListener(E type, EventListener<E, T> listener) {
    super.registerListener(type, listener);
    for (val phase : phases) {
      phase.registerListener(type, listener);
    }
  }

  @Override
  public T call() throws Exception {
    for (val phase : phases) {
      phase.execute(this, context);
    }
    return context;
  }
}
