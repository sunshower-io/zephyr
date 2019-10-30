package io.sunshower.kernel.log;

import lombok.val;

public class Logging {
  private Logging() {}

  public static Logger get(Class<?> logger) {
    val name = logger.getName();
    val delegate = java.util.logging.Logger.getLogger(name, "i18n." + name);
    return new DelegatingLoggingFacade(delegate);
  }

  /**
   * @param logger the class to log
   * @param rbSimpleName the simple name of the resource bundle to use
   * @return the logger attached to the resource-bundle at i18n/<code>logger.getPackage()</code>.
   *     <code>rbSimpleName</code>
   */
  public static Logger get(Class<?> logger, String rbSimpleName) {
    val name = logger.getName();
    val pkg = logger.getPackageName();
    val delegate = java.util.logging.Logger.getLogger(name, "i18n." + pkg + "." + rbSimpleName);
    return new DelegatingLoggingFacade(delegate);
  }
}
