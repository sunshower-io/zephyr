package io.sunshower.kernel.core;

@SuppressWarnings("PMD.FinalizeOverloaded")
public interface KernelMember {

  void initialize(Kernel kernel);

  void finalize(Kernel kernel);
}
