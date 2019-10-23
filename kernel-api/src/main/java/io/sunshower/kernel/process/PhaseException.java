package io.sunshower.kernel.process;

import io.sunshower.kernel.core.KernelException;
import io.sunshower.kernel.status.StatusException;
import lombok.Getter;

@Getter
public class PhaseException extends KernelException {

  private final Phase source;
  private final Phase.State state;

  public PhaseException(Phase.State state, Phase source, String message, Exception cause) {
    super(message, cause);
    this.state = state;
    this.source = source;
  }

  public PhaseException(Phase.State state, Phase source, String message) {
    this(state, source, message, null);
  }

  public PhaseException(Phase.State state, Phase source, Exception toException) {
    this(state, source, toException.getMessage(), toException);
  }
}
