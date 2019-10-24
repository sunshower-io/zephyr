package io.sunshower.kernel.process;

import io.sunshower.kernel.events.EventSource;

public interface Phase<E, T> extends PhaseAware<E, T>, EventSource<E, T> {

  enum State {
    /** Normal, but unrecoverable (e.g. permissions error) */
    Unrecoverable,
    /** Normal and recoverable (e.g. user forgot to create file) */
    Recoverable,
    /** Normal--no action required */
    Normal,

    /** Warning: no action <i>strictly</i> required, but fix is desirable */
    Warning,

    /** Abnormal and unrecoverable (Kernel error) */
    Error;

    public static boolean canContinue(State state) {
      return state == Normal || state == Recoverable || state == Warning;
    }
  }

  void execute(Process<E, T> process, T context);
}
