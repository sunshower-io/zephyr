package io.sunshower.kernel.events;

public interface KernelEvent<C> {

  /** @return the component that fired this event */
  C getSource();
}
