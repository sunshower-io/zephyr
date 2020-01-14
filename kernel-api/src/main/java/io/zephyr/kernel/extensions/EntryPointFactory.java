package io.zephyr.kernel.extensions;

import io.sunshower.gyre.Pair;

public interface EntryPointFactory {
  /**
   *
   * @param args the arguments passed to the main() method of zephyr
   * @return a pair containing the created entry point, and the arguments that
   * were not processed by the returned entrypoint
   */
  Pair<EntryPoint, String[]> create(String[] args);
}
