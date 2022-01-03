package io.zephyr.breeze;

public class Constants {
  static final String DEFAULT_VALUE = "..default..";

  public static boolean isDefault(String value) {
    return DEFAULT_VALUE.equals(value);
  }
}
