package io.zephyr.bundle.sfx;

public interface Log {

  Log NOOP =
      new Log() {
        @Override
        public void warn(String message, Object... args) {}

        @Override
        public void debug(String message, Object... args) {}

        @Override
        public void info(String message, Object... args) {}
      };

  void warn(String message, Object... args);

  void debug(String message, Object... args);

  void info(String message, Object... args);
}
