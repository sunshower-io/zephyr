package io.sunshower.kernel.process;

import io.sunshower.kernel.events.AbstractEventSource;
import io.sunshower.kernel.events.EventListener;
import java.util.ArrayList;
import java.util.List;
import lombok.val;

public abstract class AbstractPhase<E, T> extends AbstractEventSource<E, T> implements Phase<E, T> {

  private final List<Phase<E, T>> subphases;

  protected AbstractPhase(Class<? extends E> type) {
    super(type);
    subphases = new ArrayList<>();
  }

  @Override
  public final void addPhase(Phase<E, T> t) {
    subphases.add(t);
  }

  @Override
  public final void removePhase(Phase<E, T> t) {
    subphases.remove(t);
  }

  @Override
  public final void removeListener(E type, EventListener<E, T> listener) {
    super.removeListener(type, listener);
    for (val subphase : subphases) {
      subphase.removeListener(type, listener);
    }
  }

  @Override
  public final void registerListener(E type, EventListener<E, T> listener) {
    super.registerListener(type, listener);
    for (val subphase : subphases) {
      subphase.registerListener(type, listener);
    }
  }

  @Override
  public final void execute(Process<E, T> process, T context) {
    try {
      doExecute(process, context);
    } catch (PhaseException ex) {
      if (!State.canContinue(ex.getState())) {
        throw ex;
      }
    }
    for (val subphase : subphases) {
      subphase.execute(process, context);
    }
  }

  protected abstract void doExecute(Process<E, T> process, T context);
}
