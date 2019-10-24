package io.sunshower.kernel.process;

import io.sunshower.kernel.events.AbstractEventSource;
import io.sunshower.kernel.events.EventListener;
import io.sunshower.kernel.status.Status;
import io.sunshower.kernel.status.StatusType;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import lombok.val;

public class AbstractProcess<E, T> extends AbstractEventSource<E, T> implements Process<E, T> {

  static final Logger log = Logger.getLogger(AbstractProcess.class.getName());

  private final T context;
  private final List<Status> statuses;
  private final List<Phase<E, T>> phases;

  protected AbstractProcess(final Class<E> type, final T context) {
    super(type);
    this.context = context;
    phases = new ArrayList<>();
    statuses = new ArrayList<>();
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
  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
  public T call() throws Exception {
    for (val phase : phases) {
      try {
        phase.execute(this, context);
      } catch (PhaseException ex) {
        log.warning(ex.getMessage());
        if (!Phase.State.canContinue(ex.getState())) {
          return null;
        } else {
          //// TODO: 10/23/19 Add resolutions?
          addStatus(new Status(StatusType.WARNING, ex.getMessage(), false));
        }
      }
    }
    return context;
  }

  @Override
  public void addStatus(Status status) {
    statuses.add(status);
  }
}
