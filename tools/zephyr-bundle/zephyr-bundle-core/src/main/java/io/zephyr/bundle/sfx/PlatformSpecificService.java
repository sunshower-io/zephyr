package io.zephyr.bundle.sfx;

public interface PlatformSpecificService {

  /**
   * determine whether this service is applicable to the specified platform and architecture
   *
   * @param platform the platform we're currently considering
   * @param architecture the architecture we're currently considering
   * @return whether this service can be applied
   */
  boolean isApplicableTo(BundleOptions.Platform platform, BundleOptions.Architecture architecture);
}
