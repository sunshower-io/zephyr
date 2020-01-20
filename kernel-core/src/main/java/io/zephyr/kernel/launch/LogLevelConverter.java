package io.zephyr.kernel.launch;

import java.util.logging.Level;
import picocli.CommandLine;

public class LogLevelConverter implements CommandLine.ITypeConverter<Level> {

  @SuppressWarnings("PMD.UseLocaleWithCaseConversions")
  @Override
  public Level convert(String value) {
    try {
      return Level.parse(value.toUpperCase());
    } catch (Exception ex) {
      return Level.WARNING;
    }
  }
}
