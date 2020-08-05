package io.zephyr.bundle.sfx;

import java.util.logging.Level;

public interface Log {

  Level getLevel();

  Log NOOP =
      new Log() {
        @Override
        public Level getLevel() {
          return Level.SEVERE;
        }

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
