package io.sunshower.kernel.core;

import io.sunshower.kernel.events.EventSource;

@SuppressWarnings("PMD.FinalizeOverloaded")
public interface KernelMember extends EventSource {

  void initialize(Kernel kernel);

  void finalize(Kernel kernel);
}
