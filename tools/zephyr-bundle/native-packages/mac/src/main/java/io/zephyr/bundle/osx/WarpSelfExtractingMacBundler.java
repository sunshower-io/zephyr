package io.zephyr.bundle.osx;

import io.zephyr.bundle.sfx.AbstractSelfExecutingBundler;
import io.zephyr.bundle.sfx.BundleOptions;
import io.zephyr.bundle.sfx.SelfExecutingBundler;

public class WarpSelfExtractingMacBundler extends AbstractSelfExecutingBundler
    implements SelfExecutingBundler {
  static final String RESOURCE_PATH = "exe/mac/warp";

  public WarpSelfExtractingMacBundler() {
    super(WarpSelfExtractingMacBundler.class);
  }

  protected String getBundledResourcePath() {
    return RESOURCE_PATH;
  }

  @Override
  public boolean isApplicableTo(
      BundleOptions.Platform platform, BundleOptions.Architecture architecture) {
    return platform == BundleOptions.Platform.MacOS;
  }
}
