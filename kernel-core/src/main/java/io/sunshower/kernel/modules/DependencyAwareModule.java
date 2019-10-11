package io.sunshower.kernel.modules;

import io.sunshower.kernel.Module;
import java.io.File;

public class DependencyAwareModule implements Module {

  private final File file;

  public DependencyAwareModule(File file) {
    this.file = file;
  }

  @Override
  public ClassLoader getClassloader() {
    return null;
  }
}
