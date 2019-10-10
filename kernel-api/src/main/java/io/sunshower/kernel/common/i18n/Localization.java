package io.sunshower.kernel.common.i18n;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class Localization {

  @Getter private final ResourceBundle bundle;

  public String format(String key, Object... args) {
    return MessageFormat.format(bundle.getString(key), args);
  }
}
