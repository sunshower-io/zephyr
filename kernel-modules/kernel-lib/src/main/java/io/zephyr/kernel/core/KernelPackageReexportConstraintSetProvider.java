package io.zephyr.kernel.core;

import java.util.Set;

/**
 * Extenders may need additional or fewer packages from Zephyr's classloader. This allows
 */
public interface KernelPackageReexportConstraintSetProvider extends
    Comparable<KernelPackageReexportConstraintSetProvider> {

  /**
   * @return the mode of this provider
   */
  Mode getMode();

  /**
   * providers are ordered by their precedence.  If a provider is exporting a package that cannot be
   * tolerated, simply include an excluding provider with a higher (lower) precedence.
   *
   * @return the precedence
   */
  default int getPrecedence() {
    return 1000;
  }

  /**
   *
   * @return the set of packages to include or exclude
   */
  Set<String> getPackages();


  enum Mode {
    Include,
    Exclude
  }


}
