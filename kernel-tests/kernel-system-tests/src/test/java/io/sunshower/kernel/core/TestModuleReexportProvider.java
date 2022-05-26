package io.sunshower.kernel.core;

import io.zephyr.kernel.core.KernelPackageReexportConstraintSetProvider;
import java.util.Set;

public class TestModuleReexportProvider implements KernelPackageReexportConstraintSetProvider {

  @Override
  public Mode getMode() {
    return Mode.Include;
  }

  @Override
  public Set<String> getPackages() {
    return Set.of(".*");
  }

  @Override
  public int compareTo(
      KernelPackageReexportConstraintSetProvider kernelPackageReexportConstraintSetProvider) {
    return 0;
  }
}
