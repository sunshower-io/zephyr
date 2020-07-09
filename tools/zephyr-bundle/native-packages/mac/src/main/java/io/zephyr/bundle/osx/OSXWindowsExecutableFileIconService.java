package io.zephyr.bundle.osx;

import io.zephyr.bundle.sfx.BundleOptions;
import io.zephyr.bundle.sfx.ExecutableFileIconService;
import io.zephyr.bundle.sfx.Log;
import io.zephyr.bundle.sfx.SelfExtractingExecutableConfiguration;

public class OSXWindowsExecutableFileIconService implements ExecutableFileIconService {
  @Override
  public void setIcons(SelfExtractingExecutableConfiguration configuration, Log log) {}

  @Override
  public boolean isApplicableTo(
      BundleOptions.Platform platform, BundleOptions.Architecture architecture) {
    return platform == BundleOptions.Platform.MacOS;
  }
}
