package io.sunshower.common.i18n;

import lombok.AllArgsConstructor;

import java.text.MessageFormat;
import java.util.ResourceBundle;

@AllArgsConstructor
public class Localization {

    private final ResourceBundle bundle;

    public String format(String key, Object...args) {
        return MessageFormat.format(bundle.getString(key), args);
    }
}
