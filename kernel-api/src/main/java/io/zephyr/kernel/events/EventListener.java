package io.zephyr.kernel.events;

import lombok.val;

public interface EventListener<T> {

  final class Options {

    public static final int NONE = 1;
    public static final int REMOVE_AFTER_DISPATCH = 1 << 1;

    public static boolean isSet(int option, int... flags) {
      for (val flag : flags) {
        if ((option & flag) == option) {
          return true;
        }
      }
      return false;
    }

    public static int of(int... flags) {
      return set(0, flags);
    }

    public static int clear(int option, int... flags) {
      int result = option;
      for (val flag : flags) {
        result &= ~flag;
      }
      return result;
    }

    public static int set(int option, int... flags) {
      int result = option;
      for (val flag : flags) {
        result |= flag;
      }
      return result;
    }
  }

  void onEvent(EventType type, Event<T> event);
}
