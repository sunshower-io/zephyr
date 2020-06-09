package io.zephyr.bundle.sfx;

import java.io.File;

public interface SelfExecutingBundler {

  void load(File workspaceDirectory, Log log);

  void create(BundleOptions options, Log log);

  boolean isApplicableTo(BundleOptions.Platform platform, BundleOptions.Architecture architecture);
}
