package io.zephyr.bundle.linux;

import io.zephyr.bundle.sfx.BundleOptions;
import io.zephyr.bundle.sfx.ExecutableFileIconService;
import io.zephyr.bundle.sfx.Log;
import io.zephyr.bundle.sfx.SelfExtractingExecutableConfiguration;

public class LinuxWindowsExecutableFileIconService implements ExecutableFileIconService {
  @Override
  public boolean isApplicableTo(
      BundleOptions.Platform platform, BundleOptions.Architecture architecture) {
    return platform == BundleOptions.Platform.Linux;
  }

  @Override
  public void setIcons(SelfExtractingExecutableConfiguration configuration, Log log) {}
}
