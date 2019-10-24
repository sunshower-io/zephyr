package io.sunshower.kernel.process;

public interface PhaseAware<E, T> {
  void addPhase(Phase<E, T> t);

  void removePhase(Phase<E, T> t);
}
