package io.zephyr.logging;

import io.zephyr.kernel.Options;
import io.zephyr.kernel.core.AbstractValidatable;
import java.io.File;
import lombok.Getter;
import lombok.Setter;
import picocli.CommandLine;

public class LogOptions extends AbstractValidatable<LogOptions> implements Options<LogOptions> {

  /**
   * Specify the home directory for Zephyr. Zephyr logs are stored here. For clustered Zephyr
   * kernels, this should be a distributed directory unless a data-distribution module is installed
   */
  @Getter
  @Setter
  @CommandLine.Option(
    names = {"-h", "--home-directory"},
    defaultValue = "zephyr"
  )
  private File homeDirectory;
}
