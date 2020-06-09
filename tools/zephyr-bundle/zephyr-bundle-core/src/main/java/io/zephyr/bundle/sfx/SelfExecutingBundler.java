package io.zephyr.bundle.sfx;

import java.io.File;

public interface SelfExecutingBundler {
  void load(File workspaceDirectory);

  void create(BundleOptions options);
}
