package io.zephyr.kernel.launch;

import java.util.logging.Level;
import picocli.CommandLine;

public class LogLevelConverter implements CommandLine.ITypeConverter<Level> {

  @Override
  public Level convert(String value) throws Exception {
    return Level.parse(value.toUpperCase());
  }
}
