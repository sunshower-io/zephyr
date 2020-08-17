package io.zephyr.platform.api;

public interface NativeService {

  default boolean isNativeToCurrent() {
    return isNativeTo(Platform.current());
  }

  boolean canRunOn(Platform.OperatingSystem os);

  boolean isNativeTo(Platform.OperatingSystem os);
}
