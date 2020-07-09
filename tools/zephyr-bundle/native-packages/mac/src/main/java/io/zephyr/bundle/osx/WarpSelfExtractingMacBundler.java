package io.zephyr.bundle.osx;

import io.zephyr.bundle.sfx.BundleOptions;
import io.zephyr.bundle.sfx.Log;
import io.zephyr.bundle.sfx.SelfExecutingBundler;

import java.io.File;

public class WarpSelfExtractingMacBundler implements SelfExecutingBundler {
  @Override
  public File load(File workspaceDirectory, Log log) {
    return null;
  }

  @Override
  public File create(BundleOptions options, Log log) {
    return null;
  }

  @Override
  public boolean isApplicableTo(
      BundleOptions.Platform platform, BundleOptions.Architecture architecture) {
    return platform == BundleOptions.Platform.MacOS;
  }
}
