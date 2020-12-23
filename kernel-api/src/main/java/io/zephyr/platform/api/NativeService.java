package io.zephyr.platform.api;

import io.zephyr.api.Configurable;
import io.zephyr.api.Configuration;
import io.zephyr.api.Startable;
import io.zephyr.api.Stoppable;

public interface NativeService<T extends Configuration>
    extends Startable, Stoppable, Configurable<T> {

  boolean isRunning();

  int getProcessId();

  default boolean isNativeToCurrent() {
    return isNativeTo(Platform.current());
  }

  boolean canRunOn(Platform.OperatingSystem os);

  boolean isNativeTo(Platform.OperatingSystem os);
}
