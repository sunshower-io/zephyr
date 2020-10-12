package io.zephyr.common.io;

public class Strings {

  public static boolean isNullOrEmpty(String s) {
    if (s == null) {
      return true;
    }
    return s.isBlank();
  }
}
