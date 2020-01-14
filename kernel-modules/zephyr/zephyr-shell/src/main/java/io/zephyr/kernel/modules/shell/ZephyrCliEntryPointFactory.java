package io.zephyr.kernel.modules.shell;

import io.sunshower.gyre.Pair;
import io.zephyr.kernel.extensions.EntryPoint;
import io.zephyr.kernel.extensions.EntryPointFactory;
import lombok.val;

public class ZephyrCliEntryPointFactory implements EntryPointFactory {
  @Override
  public Pair<EntryPoint, String[]> create(String[] args) {
    val opts = parseOptions(args);
    return Pair.of(new ZephyrCliEntryPoint(opts.fst), opts.snd);
  }

  private Pair<ShellOptions, String[]> parseOptions(String[] args) {
    return null;
  }
}
