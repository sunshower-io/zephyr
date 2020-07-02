package io.zephyr.bundle.sfx;

public interface Log {

  void warn(String message, Object... args);

  void debug(String message, Object... args);

  void info(String message, Object... args);
}
