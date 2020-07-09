package io.zephyr.bundle.linux;

import io.zephyr.bundle.sfx.AbstractSelfExecutingBundler;
import io.zephyr.bundle.sfx.BundleOptions;
import io.zephyr.bundle.sfx.SelfExecutingBundler;

public class WarpSelfExecutingLinuxBundler extends AbstractSelfExecutingBundler
    implements SelfExecutingBundler {

  static final String RESOURCE_PATH = "exe/linux/warp";

  public WarpSelfExecutingLinuxBundler() {
    super(WarpSelfExecutingLinuxBundler.class);
  }

  @Override
  public boolean isApplicableTo(
      BundleOptions.Platform platform, BundleOptions.Architecture architecture) {
    return platform == BundleOptions.Platform.Linux
        && architecture == BundleOptions.Architecture.X64;
  }

  protected String getBundledResourcePath() {
    return RESOURCE_PATH;
  }
}
