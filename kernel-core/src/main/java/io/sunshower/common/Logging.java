package io.sunshower.common;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public class Logging {
  public static String message(ResourceBundle bundle, String key, Object... args) {
    return MessageFormat.format(bundle.getString(key), args);
  }
}
